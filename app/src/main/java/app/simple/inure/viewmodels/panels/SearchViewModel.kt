package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val searchKeywords: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue(SearchPreferences.getLastSearchKeyword())
        }
    }

    private val appData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadSearchData(SearchPreferences.getLastSearchKeyword())
        }
    }

    fun getSearchKeywords(): LiveData<String> {
        return searchKeywords
    }

    fun setSearchKeywords(keywords: String) {
        SearchPreferences.setLastSearchKeyword(keywords)
        searchKeywords.postValue(keywords)
        loadSearchData(keywords)
    }

    fun getSearchData(): LiveData<ArrayList<PackageInfo>> {
        return appData
    }

    fun loadSearchData(keywords: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val apps = getApplication<Application>().applicationContext.packageManager.getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

            if (keywords.isEmpty()) {
                appData.postValue(arrayListOf())
                return@launch
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

            appData.postValue(filtered)
        }
    }
}