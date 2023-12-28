package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.utils.PermissionUtils.getPermissionInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.SearchModel
import app.simple.inure.preferences.SearchPreferences
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
    private var deepApps: ArrayList<PackageInfo> = arrayListOf()
    private var thread: Thread? = null

    @Suppress("DEPRECATION")
    private var flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.GET_META_DATA or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.MATCH_UNINSTALLED_PACKAGES or
                PackageManager.MATCH_DISABLED_COMPONENTS
    } else {
        PackageManager.GET_META_DATA or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_UNINSTALLED_PACKAGES or
                PackageManager.GET_DISABLED_COMPONENTS
    }

    private val searchKeywords: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue(SearchPreferences.getLastSearchKeyword())
        }
    }

    private val searchData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    private val deepSearchData: MutableLiveData<ArrayList<SearchModel>> by lazy {
        MutableLiveData<ArrayList<SearchModel>>()
    }

    private val tags: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            loadTags()
        }
    }

    fun getSearchKeywords(): LiveData<String> {
        return searchKeywords
    }

    fun setSearchKeywords(keywords: String) {
        SearchPreferences.setLastSearchKeyword(keywords)
        searchKeywords.postValue(keywords)
        initiateSearch(keywords)
    }

    fun getSearchData(): LiveData<ArrayList<PackageInfo>> {
        return searchData
    }

    fun getDeepSearchData(): LiveData<ArrayList<SearchModel>> {
        return deepSearchData
    }

    fun getTags(): LiveData<ArrayList<String>> {
        return tags
    }

    fun initiateSearch(keywords: String) {
        thread?.interrupt()
        thread = thread(priority = 10, name = keywords) {
            try {
                if (SearchPreferences.isDeepSearchEnabled()) {
                    loadDeepSearchData(keywords)
                } else {
                    loadSearchData(keywords)
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun reload() { // These two fun already runs in their own threads
        apps.clear()
        deepApps.clear()
        refreshPackageData()
        initiateSearch(searchKeywords.value ?: "")
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadSearchData(keywords: String) {
        var apps = (getInstalledApps() + getUninstalledApps()).toArrayList()

        if (keywords.isEmpty()) {
            searchData.postValue(arrayListOf())
            return
        }

        apps = if (keywords.startsWith("#")) {
            val tagsDatabase = TagsDatabase.getInstance(application.applicationContext)
            val tag = keywords.substring(1)
            val tagApps = tagsDatabase?.getTagDao()?.getTag(tag)?.packages?.split(",")

            apps.parallelStream().filter { p ->
                tagApps?.contains(p.packageName) ?: false
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>
        } else {
            apps.parallelStream().filter { p ->
                p.applicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored())
                        || p.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>
        }

        when (SearchPreferences.getAppsCategory()) {
            SortConstant.SYSTEM -> {
                apps = apps.parallelStream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            SortConstant.USER -> {
                apps = apps.parallelStream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
        }

        var filteredList = arrayListOf<PackageInfo>()

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.COMBINE_FLAGS)) { // Pretty special case, even I don't know what I did here
            filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).parallelStream().filter { p ->
                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                        true
                    } else {
                        p.applicationInfo.enabled.invert()
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                        true
                    } else {
                        p.applicationInfo.enabled
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                        true
                    } else {
                        p.applicationInfo.splitSourceDirs.isNullOrEmpty()
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                        true
                    } else {
                        p.applicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                    p.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
                } else {
                    true
                }
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
        } else {
            for (app in apps) {
                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    if (app.applicationInfo.enabled.invert()) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    if (app.applicationInfo.enabled) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                    if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    if (app.applicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                    if (app.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }
            }

            // Remove duplicate elements
            filteredList = filteredList.parallelStream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
        }

        filteredList.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        if (Thread.currentThread().name == thread?.name) {
            searchData.postValue(filteredList)
        }
    }

    private fun loadDeepSearchData(keywords: String) {
        var list = arrayListOf<SearchModel>()
        var apps: ArrayList<PackageInfo>

        if (keywords.isEmpty()) {
            deepSearchData.postValue(arrayListOf())
            return
        }

        if (deepApps.isEmpty()) {
            deepApps = packageManager.getInstalledPackages(flags.toLong()).loadPackageNames()
        }

        @Suppress("UNCHECKED_CAST")
        apps = deepApps.clone() as ArrayList<PackageInfo>

        when (SearchPreferences.getAppsCategory()) {
            SortConstant.SYSTEM -> {
                apps = apps.parallelStream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            SortConstant.USER -> {
                apps = apps.parallelStream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
        }

        var filteredList = arrayListOf<PackageInfo>()

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.COMBINE_FLAGS)) { // Pretty special case, even I don't know what I did here
            @Suppress("UNCHECKED_CAST")
            filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).parallelStream().filter { p ->
                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                        true
                    } else {
                        p.applicationInfo.enabled.invert()
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                        true
                    } else {
                        p.applicationInfo.enabled
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                        true
                    } else {
                        p.applicationInfo.splitSourceDirs.isNullOrEmpty()
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                        true
                    } else {
                        p.applicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                    }
                } else {
                    true
                } && if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                    p.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
                } else {
                    true
                }
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
        } else {
            for (app in apps) {
                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                    if (app.applicationInfo.enabled.invert()) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                    if (app.applicationInfo.enabled) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                    if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                    if (app.applicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }

                if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                    if (app.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                        if (!filteredList.contains(app)) {
                            filteredList.add(app)
                        }
                    }
                }
            }

            // Remove duplicate elements
            filteredList = filteredList.parallelStream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
        }

        filteredList.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        for (app in filteredList) { // Split this into multiple threads
            val searchModel = SearchModel()

            searchModel.packageInfo = app
            searchModel.permissions = getPermissionCount(keywords, app)
            searchModel.activities = getActivitiesCount(keywords, app)
            searchModel.services = getServicesCount(keywords, app)
            searchModel.receivers = getReceiversCount(keywords, app)
            searchModel.providers = getProvidersCount(keywords, app)
            searchModel.resources = getResourcesCount(keywords, app)

            list.add(searchModel)
        }

        // Filter out apps with no results

        list = list.filter {
            it.permissions > 0 || it.activities > 0 || it.services > 0 ||
                    it.receivers > 0 || it.providers > 0 || it.resources > 0 ||
                    it.packageInfo.applicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored()) ||
                    it.packageInfo.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
        } as ArrayList<SearchModel>

        if (Thread.currentThread().name == thread?.name) {
            deepSearchData.postValue(list)
        }
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsDatabase = TagsDatabase.getInstance(application.applicationContext)
            tags.postValue(tagsDatabase?.getTagDao()?.getTagsNameOnly()?.toArrayList() ?: arrayListOf())
        }
    }

    private fun getPermissionCount(keyword: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            if (app.requestedPermissions != null) {
                for (permission in app.requestedPermissions) {
                    if (permission.lowercase().contains(keyword.lowercase())
                        || permission.getPermissionInfo(application)?.loadLabel(application.packageManager)
                            .toString().lowercase().contains(keyword.lowercase())) {
                        count = count.inc()
                    }
                }
            }
        }

        return count
    }

    private fun getActivitiesCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            if (app.activities != null) {
                for (i in app.activities) {
                    if (i.name.lowercase().contains(keywords.lowercase())) {
                        count = count.inc()
                    }
                }
            }
        }

        return count
    }

    private fun getServicesCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            if (app.services != null) {
                for (i in app.services) {
                    if (i.name.lowercase().contains(keywords.lowercase())) {
                        count = count.inc()
                    }
                }
            }
        }

        return count
    }

    private fun getReceiversCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            if (app.receivers != null) {
                for (i in app.receivers) {
                    if (i.name.lowercase().contains(keywords.lowercase())) {
                        count = count.inc()
                    }
                }
            }
        }

        return count
    }

    private fun getProvidersCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            if (app.providers != null) {
                for (i in app.providers) {
                    if (i.name.lowercase().contains(keywords.lowercase())) {
                        count = count.inc()
                    }
                }
            }
        }

        return count
    }

    private fun getResourcesCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            with(APKParser.getXmlFiles(app.applicationInfo.sourceDir, keywords)) {
                count = count()
            }
        }

        return count
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            thread?.interrupt()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun clearSearch() {
        initiateSearch("")
    }
}