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
import app.simple.inure.apk.parsers.APKParser
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
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class SearchViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var deepPackageInfos: ArrayList<PackageInfo> = arrayListOf()
    private var searchJobs: MutableSet<Job> = mutableSetOf()

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
        filteredApps.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        ensureActive()

        val searchResults = if (sanitizedKeyword.isNotEmpty()) {
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

        ensureActive()
        searchData.postValue(ArrayList(searchResults))
    }

    private fun hasValidCounts(search: Search): Boolean {
        return search.permissions > 0 || search.activities > 0 || search.services > 0 ||
                search.receivers > 0 || search.providers > 0 || search.resources > 0
    }

    private fun hasMatchingNames(search: Search, keywords: String): Boolean {
        return search.packageInfo.safeApplicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored()) ||
                search.packageInfo.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsDatabase = TagsDatabase.getInstance(application.applicationContext)
            tags.postValue(tagsDatabase?.getTagDao()?.getTagsNameOnly()?.toArrayList() ?: arrayListOf())
        }
    }

    private suspend fun ArrayList<Search>.addDeepSearchData(keywords: String, deepSearchData: ArrayList<PackageInfo>) = coroutineScope {
        try {
            deepSearchData.map { packageInfo ->
                async {
                    ensureActive()
                    try {
                        val search = Search()

                        search.packageInfo = packageInfo
                        search.permissions = packageInfo.requestedPermissions?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it!! } ?: 0
                        search.activities = packageInfo.activities?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                        search.services = packageInfo.services?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                        search.receivers = packageInfo.receivers?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                        search.providers = packageInfo.providers?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                        search.resources = APKParser.getXmlFiles(packageInfo.safeApplicationInfo.sourceDir, keywords, SearchPreferences.isCasingIgnored()).size

                        addIfNotExists(search, comparator = { a, b -> a?.packageInfo?.packageName == b?.packageInfo?.packageName })
                    } catch (e: NameNotFoundException) {
                        Log.e(TAG, e.stackTraceToString())
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
                val pkg = packageManager.getPackageInfo(it.packageName, FLAGS)
                pkg.safeApplicationInfo.name = it.safeApplicationInfo.name
                deepPackageInfos.addIfNotExists(pkg, comparator = { a, b -> a?.packageName == b?.packageName })
            }.getOrElse {
                Log.e(TAG, it.stackTraceToString())
            }
        }
    }

    private fun ArrayList<PackageInfo>.applyFilters(filtered: ArrayList<PackageInfo>) {
        parallelStream().forEach { app ->
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
    }

    private fun ArrayList<PackageInfo>.filterCategories(keywords: String): ArrayList<PackageInfo> {
        when {
            keywords.startsWith("#") -> {
                val tagsDatabase = TagsDatabase.getInstance(application.applicationContext)
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
    }
}
