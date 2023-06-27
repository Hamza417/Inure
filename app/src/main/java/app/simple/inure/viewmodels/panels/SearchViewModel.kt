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
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.SearchModel
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class SearchViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()

    @Suppress("DEPRECATION")
    private var flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.GET_META_DATA or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.MATCH_UNINSTALLED_PACKAGES
    } else {
        PackageManager.GET_META_DATA or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_UNINSTALLED_PACKAGES
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

    fun initiateSearch(keywords: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (SearchPreferences.isDeepSearchEnabled()) {
                loadDeepSearchData(keywords)
            } else {
                loadSearchData(keywords)
            }
        }
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            apps.clear()
            refreshPackageData()
            initiateSearch(searchKeywords.value!!)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadSearchData(keywords: String) {
        var apps = getInstalledApps()

        if (keywords.isEmpty()) {
            searchData.postValue(arrayListOf())
            return
        }

        apps = apps.stream().filter { p ->
            p.applicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored())
                    || p.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
        }.collect(Collectors.toList()) as ArrayList<PackageInfo>

        when (SearchPreferences.getAppsCategory()) {
            SortConstant.SYSTEM -> {
                apps = apps.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            SortConstant.USER -> {
                apps = apps.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
        }

        var filteredList = arrayListOf<PackageInfo>()

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.COMBINE_FLAGS)) { // Pretty special case, even I don't know what I did here
            filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
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
            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    p.applicationInfo.enabled.invert()
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    p.applicationInfo.enabled
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    p.applicationInfo.splitSourceDirs.isNullOrEmpty()
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    p.applicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            }

            if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            }

            // Remove duplicate elements
            filteredList = filteredList.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
        }

        filteredList.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        searchData.postValue(filteredList)
    }

    private fun loadDeepSearchData(keywords: String) {
        var list = arrayListOf<SearchModel>()
        var apps = getInstalledApps()

        if (keywords.isEmpty()) {
            deepSearchData.postValue(arrayListOf())
            return
        }

        when (SearchPreferences.getAppsCategory()) {
            SortConstant.SYSTEM -> {
                apps = apps.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            SortConstant.USER -> {
                apps = apps.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
        }

        apps.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        for (app in apps) {
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

        list = list.filter {
            it.permissions > 0 || it.activities > 0 || it.services > 0 || it.receivers > 0 || it.providers > 0 || it.resources > 0 ||
                    it.packageInfo.applicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored()) ||
                    it.packageInfo.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
        } as ArrayList<SearchModel>

        deepSearchData.postValue(list)
    }

    private fun getPermissionCount(keyword: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            if (app.requestedPermissions != null) {
                for (permission in app.requestedPermissions) {
                    if (permission.lowercase().contains(keyword.lowercase())
                        || permission.getPermissionInfo(application)?.loadLabel(application.packageManager).toString().lowercase().contains(keyword.lowercase())) {
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
}