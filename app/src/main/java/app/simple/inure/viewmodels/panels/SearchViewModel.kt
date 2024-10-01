package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.utils.PackageUtils.isSystemApp
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
import kotlinx.coroutines.launch
import java.util.stream.Collectors
import kotlin.concurrent.thread

class SearchViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var deepPackageInfos: ArrayList<PackageInfo> = arrayListOf()
    private var thread: Thread? = null

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
        thread?.interrupt()
        thread = thread(priority = 10, name = keywords) {
            try {
                searchKeywords.postValue(keywords)
                loadSearchData(keywords)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        if (Thread.currentThread() != Looper.getMainLooper().thread) {
            thread?.join() // Wait for the previous thread to finish
        }
    }

    fun reload() { // These two fun already runs in their own threads
        deepPackageInfos.clear()
        apps.clear()
        refreshPackageData()
    }

    private fun loadSearchData(keywords: String) {
        var list = arrayListOf<Search>()
        var apps: ArrayList<PackageInfo> = (getInstalledApps() + getUninstalledApps()).toArrayList()
        val filteredList = arrayListOf<PackageInfo>()

        if (keywords.isEmpty()) {
            searchData.postValue(arrayListOf())
            return
        }

        val sanitizedKeyword = if (keywords.startsWith("#")) {
            try {
                keywords.split(" ")[1]
            } catch (e: IndexOutOfBoundsException) {
                ""
            }
        } else {
            keywords
        }

        apps = apps.filterCategories(keywords)
        apps.applyFilters(filteredList)
        filteredList.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        if (sanitizedKeyword.isNotEmpty()) {
            when {
                SearchPreferences.isDeepSearchEnabled() -> {
                    if (deepPackageInfos.isEmpty()) {
                        loadDataForDeepSearch(filteredList)
                    }

                    list.addDeepSearchData(sanitizedKeyword, deepPackageInfos)

                    // Filter out apps with no results
                    list = if (list.isNotEmpty()) {
                        list.filter { search ->
                            hasValidCounts(search) || hasMatchingNames(search, sanitizedKeyword)
                        } as ArrayList<Search>
                    } else {
                        arrayListOf()
                    }
                }
                else -> {
                    list.addAll(filteredList.map { Search(it) }.filter { search ->
                        hasMatchingNames(search, sanitizedKeyword)
                    } as ArrayList<Search>)
                }
            }
        } else {
            list.addAll(filteredList.map { Search(it) } as ArrayList<Search>)
        }

        if (Thread.currentThread().name == thread?.name) {
            searchData.postValue(list)
        }
    }

    private fun hasValidCounts(search: Search): Boolean {
        return search.permissions > 0 || search.activities > 0 || search.services > 0 ||
                search.receivers > 0 || search.providers > 0 || search.resources > 0
    }

    private fun hasMatchingNames(search: Search, keywords: String): Boolean {
        return search.packageInfo.applicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored()) ||
                search.packageInfo.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsDatabase = TagsDatabase.getInstance(application.applicationContext)
            tags.postValue(tagsDatabase?.getTagDao()?.getTagsNameOnly()?.toArrayList() ?: arrayListOf())
        }
    }

    private fun ArrayList<Search>.addDeepSearchData(keywords: String, deepSearchData: ArrayList<PackageInfo>) {
        deepSearchData.parallelStream().forEach { it ->
            try {
                val search = Search()

                search.packageInfo = it
                search.permissions = it.requestedPermissions?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it!! } ?: 0
                search.activities = it.activities?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                search.services = it.services?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                search.receivers = it.receivers?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                search.providers = it.providers?.getMatchedCount(keywords, SearchPreferences.isCasingIgnored()) { it?.name!! } ?: 0
                search.resources = APKParser.getXmlFiles(it.applicationInfo.sourceDir, keywords, SearchPreferences.isCasingIgnored()).size

                addIfNotExists(search, comparator = { a, b -> a?.packageInfo?.packageName == b?.packageInfo?.packageName })
            } catch (e: NameNotFoundException) {
                Log.e(TAG, e.stackTraceToString())
            }
        }
    }

    private fun loadDataForDeepSearch(list: ArrayList<PackageInfo>) {
        list.forEach {
            kotlin.runCatching {
                val pkg = packageManager.getPackageInfo(it.packageName, FLAGS)
                pkg.applicationInfo.name = it.applicationInfo.name
                deepPackageInfos.addIfNotExists(pkg, comparator = { a, b -> a?.packageName == b?.packageName })
            }.getOrElse {
                Log.e(TAG, it.stackTraceToString())
            }
        }
    }

    private fun ArrayList<PackageInfo>.applyFilters(filtered: ArrayList<PackageInfo>) {
        parallelStream().forEach { app ->
            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                if (app.applicationInfo.enabled.invert()) {
                    filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                }
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                if (app.applicationInfo.enabled) {
                    filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                }
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                }
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                if (app.applicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                    filtered.addIfNotExists(app, comparator = { a, b -> a?.packageName == b?.packageName })
                }
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                if (app.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
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
            thread?.interrupt()
            Log.d("SearchViewModel", "onCleared: ${thread?.name}")
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun clearSearch() {
        searchKeywords.postValue("")
        searchData.postValue(arrayListOf())

        try {
            thread?.interrupt()
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
