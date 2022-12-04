package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.SearchModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.*
import java.util.stream.Collectors

class SearchViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var searchJob: Job? = null

    private val searchKeywords: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue(SearchPreferences.getLastSearchKeyword())
        }
    }

    private val searchData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            initiateSearch(SearchPreferences.getLastSearchKeyword())
        }
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
        searchJob?.cancel(CancellationException("new search data requested"))
        if (searchJob?.isActive == true) {
            Log.e("SearchViewModel", "loadSearchData: job is still active")
        }

        viewModelScope.launch(Dispatchers.IO) {
            loadSearchData(keywords)
        }
    }

    private suspend fun loadSearchData(keywords: String) {
        searchJob?.join()
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
            yield()
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
        var c = 0
        kotlin.runCatching {
            for (count in app.requestedPermissions.indices) {
                if (app.requestedPermissions[count].lowercase().contains(keyword.lowercase())) {
                    c += 1
                }
            }
        }

        return c
    }

    private fun getActivitiesCount(keywords: String, app: PackageInfo): Int {
        var c = 0
        kotlin.runCatching {
            for (i in app.activities) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    c += 1
                }
            }
        }

        return c
    }

    private fun getServicesCount(keywords: String, app: PackageInfo): Int {
        var c = 0
        kotlin.runCatching {
            for (i in app.services) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    c += 1
                }
            }
        }

        return c
    }

    private fun getReceiversCount(keywords: String, app: PackageInfo): Int {
        var c = 0
        kotlin.runCatching {
            for (i in app.receivers) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    c += 1
                }
            }
        }

        return c
    }

    private fun getProvidersCount(keywords: String, app: PackageInfo): Int {
        var c = 0
        kotlin.runCatching {
            for (i in app.providers) {
                if (i.name.lowercase().contains(keywords.lowercase())) {
                    c += 1
                }
            }
        }

        return c
    }

    private fun getResourcesCount(keywords: String, app: PackageInfo): Int {
        var c = 0
        kotlin.runCatching {
            with(APKParser.getXmlFiles(app.applicationInfo.sourceDir, keywords)) {
                c = count()
            }
        }

        return c
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        initiateSearch(SearchPreferences.getLastSearchKeyword())
    }
}