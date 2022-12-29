package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.SearchModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.stream.Collectors

class SearchViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val searchKeywords: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue(SearchPreferences.getLastSearchKeyword())
        }
    }

    private val searchData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadPackageData()
        }
    }

    private val deepSearchData: MutableLiveData<ArrayList<SearchModel>> by lazy {
        MutableLiveData<ArrayList<SearchModel>>().also {
            loadPackageData()
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

    fun initiateSearch(keywords: String) {
        viewModelScope.launch(Dispatchers.IO) {
            loadSearchData(keywords)
            this.coroutineContext.job.join()
        }
    }

    private suspend fun loadSearchData(keywords: String) {
        val apps = getInstalledApps()

        if (keywords.isEmpty()) {
            if (SearchPreferences.isDeepSearchEnabled()) {
                deepSearchData.postValue(arrayListOf())
                return
            } else {
                searchData.postValue(arrayListOf())
                return
            }
        }

        for (i in apps.indices) {
            apps[i].applicationInfo.name = getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
        }

        var filtered: ArrayList<PackageInfo> =
            apps.stream().filter { p ->
                p.applicationInfo.name.contains(keywords, SearchPreferences.isCasingIgnored())
                        || p.packageName.contains(keywords, SearchPreferences.isCasingIgnored())
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>

        when (SearchPreferences.getAppsCategory()) {
            PopupAppsCategory.SYSTEM -> {
                filtered = filtered.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            PopupAppsCategory.USER -> {
                filtered = filtered.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
        }

        filtered.getSortedList(SearchPreferences.getSortStyle(), SearchPreferences.isReverseSorting())

        if (SearchPreferences.isDeepSearchEnabled()) {
            loadDeepSearchData(keywords, filtered)
        } else {
            searchData.postValue(filtered)
        }
    }

    private suspend fun loadDeepSearchData(keywords: String, apps: ArrayList<PackageInfo>) {
        val list = arrayListOf<SearchModel>()

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

        yield()
        deepSearchData.postValue(list)
    }

    private fun getPermissionCount(keyword: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            for (index in app.requestedPermissions.indices) {
                if (app.requestedPermissions[index].lowercase().contains(keyword.lowercase())) {
                    count += 1
                }
            }
        }

        return count
    }

    private fun getActivitiesCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            for (i in app.activities) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    count += 1
                }
            }
        }

        return count
    }

    private fun getServicesCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            for (i in app.services) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    count += 1
                }
            }
        }

        return count
    }

    private fun getReceiversCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            for (i in app.receivers) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    count += 1
                }
            }
        }

        return count
    }

    private fun getProvidersCount(keywords: String, app: PackageInfo): Int {
        var count = 0

        kotlin.runCatching {
            for (i in app.providers) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    count += 1
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
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }
}