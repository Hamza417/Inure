package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class SearchData(application: Application) : AndroidViewModel(application) {

    private val searchKeywords: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.postValue(SearchPreferences.getLastSearchKeyword())
            loadSearchData()
        }
    }

    private val appData: MutableLiveData<ArrayList<ApplicationInfo>> by lazy {
        MutableLiveData<ArrayList<ApplicationInfo>>().also {
            loadSearchData()
        }
    }

    fun getSearchKeywords(): LiveData<String> {
        return searchKeywords
    }

    fun setSearchKeywords(keywords: String) {
        SearchPreferences.setLastSearchKeyword(keywords)
        searchKeywords.postValue(keywords)
    }

    fun getSearchData(): LiveData<ArrayList<ApplicationInfo>> {
        return appData
    }

    fun loadSearchData() {
        viewModelScope.launch(Dispatchers.Default) {

            val apps = getApplication<Application>().applicationContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList

            if (searchKeywords.value.isNullOrEmpty()) {
                appData.postValue(arrayListOf())
                return@launch
            }

            for (i in apps.indices) {
                apps[i].name = getApplicationName(getApplication<Application>().applicationContext, apps[i])
            }

            var filtered: ArrayList<ApplicationInfo> =
                apps.stream().filter { p ->
                    p.name.contains(searchKeywords.value!!, true)
                            || p.packageName.contains(searchKeywords.value!!, true)
                }.collect(Collectors.toList()) as ArrayList<ApplicationInfo>

            when (MainPreferences.getListAppCategory()) {
                AppCategoryPopup.SYSTEM -> {
                    filtered = filtered.stream().filter { p ->
                        p.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<ApplicationInfo>
                }
                AppCategoryPopup.USER -> {
                    filtered = filtered.stream().filter { p ->
                        p.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<ApplicationInfo>
                }
            }

            filtered.getSortedList(MainPreferences.getSortStyle(), getApplication<Application>().applicationContext)

            appData.postValue(filtered)
        }
    }
}
