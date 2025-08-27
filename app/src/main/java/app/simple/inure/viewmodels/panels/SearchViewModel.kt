package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser.getMatchedResourcesSize
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.apk.utils.PackageUtils.isSystemApp
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.Search
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.ArrayUtils.addIfNotExists
import app.simple.inure.util.ArrayUtils.getMatchedCount
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort
import app.simple.inure.util.Sort.getSortedList
import app.simple.inure.viewmodels.panels.SearchViewModel.Companion.MAX_PARALLEL_STREAMS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.Locale
import java.util.stream.Collectors

class SearchViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var deepPackageInfos: ArrayList<PackageInfo> = arrayListOf()
    private var searchJobs: MutableSet<Job> = mutableSetOf()
    private val semaphore = Semaphore(MAX_PARALLEL_STREAMS)

    private val searchKeywords: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue(SearchPreferences.getLastSearchKeyword())
        }
    }

    private val searchData: MutableLiveData<ArrayList<Search>> by lazy {
        MutableLiveData<ArrayList<Search>>()
    }

    private val tags: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            loadTags()
        }
    }

    fun getSearchKeywords(): LiveData<String> {
        return searchKeywords
    }

    fun getSearchData(): LiveData<ArrayList<Search>> {
        return searchData
    }

    fun getTags(): LiveData<ArrayList<String>> {
        return tags
    }

    fun shouldShowLoader(): Boolean {
        return searchData.value.isNullOrEmpty()
    }

    fun initiateSearch(keywords: String) {
        searchJobs.forEach { it.cancel() }

        val job = viewModelScope.launch(Dispatchers.IO) {
            try {
                searchKeywords.postValue(keywords)
                ensureActive()
                loadSearchData(keywords)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: CancellationException) {
                e.printStackTrace()
            }
        }

        searchJobs.add(job)
    }

    fun reload() { // These two fun already runs in their own threads
        deepPackageInfos.clear()
        apps.clear()
        refreshPackageData()
    }

    private suspend fun loadSearchData(keywords: String) = coroutineScope {
        if (keywords.isEmpty()) {
            searchData.postValue(arrayListOf())
            return@coroutineScope
        }

        ensureActive()

        val sanitizedKeyword = keywords.split(" ").getOrNull(1).takeIf { keywords.startsWith("#") } ?: keywords
        val allApps = (getInstalledApps() + getUninstalledApps()).toArrayList()
        val filteredApps = arrayListOf<PackageInfo>()

        allApps.filterCategories(keywords).applyFilters(filteredApps)
        if (SearchPreferences.getSortStyle() == Sort.RELEVANCE) {
            // We'll sort by Levenshtein relevance after building Search results below
        } else {
            filteredApps.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())
        }

        ensureActive()

        val searchResultsBase = if (sanitizedKeyword.isNotEmpty()) {
            if (SearchPreferences.isDeepSearchEnabled()) {
                if (deepPackageInfos.isEmpty()) {
                    loadDataForDeepSearch(filteredApps)
                }
                val deepSearchResults = arrayListOf<Search>().apply {
                    addDeepSearchData(sanitizedKeyword, deepPackageInfos)
                }
                deepSearchResults.filter { hasValidCounts(it) || hasMatchingNames(it, sanitizedKeyword) }
            } else {
                filteredApps.map { Search(it) }.filter { hasMatchingNames(it, sanitizedKeyword) }
            }
        } else {
            filteredApps.map { Search(it) }
        }

        // Apply Levenshtein relevance sorting if selected and we have a non-empty keyword
        val finalResults = if (SearchPreferences.getSortStyle() == Sort.RELEVANCE && sanitizedKeyword.isNotEmpty()) {
            val ignoreCase = SearchPreferences.isCasingIgnored()
            // Precompute scores to avoid recomputation and enable deterministic tie-breaking
            val scored = searchResultsBase.map { it to relevanceScore(it, sanitizedKeyword, ignoreCase) }
            val comparator = compareByDescending<Pair<Search, Double>> { it.second }
                .thenBy {
                    val name = it.first.packageInfo.safeApplicationInfo.name
                    if (ignoreCase) name.lowercase(Locale.getDefault()) else name
                }
                .thenBy { it.first.packageInfo.packageName }

            val sorted = if (SearchPreferences.isReverseSorting()) {
                scored.sortedWith(comparator.reversed())
            } else {
                scored.sortedWith(comparator)
            }
            sorted.map { it.first }
        } else {
            searchResultsBase
        }

        ensureActive()
        searchData.postValue(ArrayList(finalResults))
    }

    private fun hasValidCounts(search: Search): Boolean {
        return search.permissions > 0 || search.activities > 0 || search.services > 0 ||
                search.receivers > 0 || search.providers > 0 || search.resources > 0
    }

    private fun hasMatchingNames(search: Search, keywords: String): Boolean {
        return search.packageInfo.safeApplicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored()) ||
                search.packageInfo.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
    }

    // Compute a relevance score [0.0, 1.0+] for a Search item based on Levenshtein similarity to app label and package name
    private fun relevanceScore(search: Search, query: String, ignoreCase: Boolean): Double {
        val label = search.packageInfo.safeApplicationInfo.name
        val pkg = search.packageInfo.packageName

        val labelSim = similarity(query, label, ignoreCase)
        val pkgSim = similarity(query, pkg, ignoreCase)

        var score = maxOf(labelSim, pkgSim)

        // If deep search is enabled, include best similarity from matched components
        if (SearchPreferences.isDeepSearchEnabled()) {
            val compSim = componentBestSimilarity(search, query, ignoreCase)
            score = maxOf(score, compSim)

            // Tiny nudge if any deep resources matched
            if (search.resources > 0) score += 0.02
        }

        // Apply intuitive bonuses based on the primary highest-similarity text
        val primaryText = when (score) {
            labelSim -> label
            pkgSim -> pkg
            else -> null
        } ?: label

        if (primaryText.equals(query, ignoreCase)) score += 0.30
        else if (primaryText.startsWith(query, ignoreCase)) score += 0.15
        else if (primaryText.contains(query, ignoreCase)) score += 0.05

        return score
    }

    /**
     * For deep search, compute the best Levenshtein similarity among matched component names
     * (only those containing the query), across permissions, activities, services, receivers, providers.
     */
    private fun componentBestSimilarity(search: Search, query: String, ignoreCase: Boolean): Double {
        val pi = search.packageInfo
        var best = 0.0

        // Permissions
        pi.requestedPermissions?.forEach { perm ->
            if (perm != null && perm.contains(query, ignoreCase)) {
                best = maxOf(best, similarity(query, perm, ignoreCase))
            }
        }

        // Activities
        pi.activities?.forEach { ai ->
            val name = ai?.name
            if (!name.isNullOrEmpty() && name.contains(query, ignoreCase)) {
                best = maxOf(best, similarity(query, name, ignoreCase))
            }
        }

        // Services
        pi.services?.forEach { si ->
            val name = si?.name
            if (!name.isNullOrEmpty() && name.contains(query, ignoreCase)) {
                best = maxOf(best, similarity(query, name, ignoreCase))
            }
        }

        // Receivers
        pi.receivers?.forEach { ri ->
            val name = ri?.name
            if (!name.isNullOrEmpty() && name.contains(query, ignoreCase)) {
                best = maxOf(best, similarity(query, name, ignoreCase))
            }
        }

        // Providers
        pi.providers?.forEach { pr ->
            val name = pr?.name
            if (!name.isNullOrEmpty() && name.contains(query, ignoreCase)) {
                best = maxOf(best, similarity(query, name, ignoreCase))
            }
        }

        return best
    }

    // Normalized Levenshtein similarity in [0.0, 1.0], 1.0 is identical
    private fun similarity(a: String, b: String, ignoreCase: Boolean): Double {
        val aa = if (ignoreCase) a.lowercase(Locale.getDefault()) else a
        val bb = if (ignoreCase) b.lowercase(Locale.getDefault()) else b
        if (aa.isEmpty() && bb.isEmpty()) return 1.0
        if (aa.isEmpty() || bb.isEmpty()) return 0.0
        val d = levenshtein(aa, bb).toDouble()
        val denom = maxOf(aa.length, bb.length).toDouble()
        return 1.0 - (d / denom)
    }

    // Standard DP Levenshtein distance
    private fun levenshtein(a: String, b: String): Int {
        val n = a.length
        val m = b.length
        if (n == 0) return m
        if (m == 0) return n

        val prev = IntArray(m + 1) { it }
        val curr = IntArray(m + 1)

        for (i in 1..n) {
            curr[0] = i
            val ca = a[i - 1]
            for (j in 1..m) {
                val cost = if (ca == b[j - 1]) 0 else 1
                curr[j] = minOf(
                        curr[j - 1] + 1,      // insertion
                        prev[j] + 1,          // deletion
                        prev[j - 1] + cost    // substitution
                )
            }
            // swap arrays
            for (j in 0..m) prev[j] = curr[j]
        }
        return prev[m]
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsDatabase = TagsDatabase.getInstance(applicationContext())
            tags.postValue(tagsDatabase?.getTagDao()?.getTagsNameOnly()?.toArrayList() ?: arrayListOf())
        }
    }

    private suspend fun ArrayList<Search>.addDeepSearchData(keywords: String, deepSearchData: ArrayList<PackageInfo>) = coroutineScope {
        try {
            deepSearchData.map { packageInfo ->
                async {
                    ensureActive()

                    /**
                     * I think parcel has a limit of how much data it can pass at once without crashing
                     * so I'm using a semaphore to limit the matching to [MAX_PARALLEL_STREAMS] at a time.
                     */
                    semaphore.withPermit {
                        try {
                            val search = Search()
                            ensureActive()

                            search.packageInfo = packageInfo
                            search.permissions = packageInfo.requestedPermissions
                                ?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it!! } ?: 0
                            search.activities = packageInfo.activities
                                ?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                            search.services = packageInfo.services
                                ?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                            search.receivers = packageInfo.receivers
                                ?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                            search.providers = packageInfo.providers
                                ?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                            search.resources = packageInfo.getMatchedResourcesSize(keywords, SearchPreferences.isCasingIgnored())

                            addIfNotExists(search, comparator = { a, b ->
                                a?.packageInfo?.packageName == b?.packageInfo?.packageName
                            })
                        } catch (e: NameNotFoundException) {
                            Log.e(TAG, e.stackTraceToString())
                        }
                    }
                }
            }.awaitAll()
        } catch (e: ConcurrentModificationException) {
            e.printStackTrace()
        }
    }

    private fun loadDataForDeepSearch(list: ArrayList<PackageInfo>) {
        list.forEach {
            kotlin.runCatching {
                val packageInfo = packageManager.getPackageInfo(it.packageName, FLAGS)
                packageInfo.safeApplicationInfo.name = it.safeApplicationInfo.name
                deepPackageInfos.addIfNotExists(packageInfo, comparator = { a, b -> a?.packageName == b?.packageName })
            }.getOrElse {
                Log.e(TAG, it.stackTraceToString())
            }
        }
    }

    private suspend fun ArrayList<PackageInfo>.applyFilters(filtered: ArrayList<PackageInfo>) = coroutineScope {
        map { app ->
            ensureActive()

            async {
                ensureActive()

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    if (app.safeApplicationInfo.enabled.invert()) {
                        if (app.isInstalled()) {
                            filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    if (app.safeApplicationInfo.enabled) {
                        if (app.isInstalled()) {
                            filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                    if (app.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    if (app.safeApplicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                        filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                    if (app.isInstalled().invert()) {
                        filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                    }
                }
            }
        }.awaitAll()
    }

    private fun ArrayList<PackageInfo>.filterCategories(keywords: String): ArrayList<PackageInfo> {
        when {
            keywords.startsWith("#") -> {
                val tagsDatabase = TagsDatabase.getInstance(applicationContext())
                val tag = keywords.split(" ")[0].substring(1)
                val tagApps = tagsDatabase?.getTagDao()?.getTag(tag)?.packages?.split(",")

                return parallelStream().filter { packageInfo ->
                    tagApps?.contains(packageInfo.packageName) == true
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            else -> {
                when (SearchPreferences.getAppsCategory()) {
                    SortConstant.SYSTEM -> {
                        return parallelStream().filter { packageInfo ->
                            packageInfo.isSystemApp()
                        }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                    }
                    SortConstant.USER -> {
                        return parallelStream().filter { packageInfo ->
                            packageInfo.isSystemApp().invert()
                        }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                    }
                }
            }
        }

        return this
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        initiateSearch(searchKeywords.value ?: SearchPreferences.getLastSearchKeyword())
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            searchJobs.forEach { it.cancel() }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun clearSearch() {
        searchKeywords.postValue("")
        searchData.postValue(arrayListOf())

        try {
            searchJobs.forEach { it.cancel() }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    companion object {
        @Suppress("DEPRECATION")
        private var FLAGS = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                PackageManager.GET_META_DATA or
                        PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.MATCH_UNINSTALLED_PACKAGES or
                        PackageManager.MATCH_DISABLED_COMPONENTS
            }
            else -> {
                PackageManager.GET_META_DATA or
                        PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_UNINSTALLED_PACKAGES or
                        PackageManager.GET_DISABLED_COMPONENTS
            }
        }

        private const val TAG = "SearchViewModel"
        private const val MAX_PARALLEL_STREAMS = 10
    }
}
