package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.packagehelper.PackageUtils.getApplicationName
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun getSearchKeywords() : LiveData<String> {
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
        CoroutineScope(Dispatchers.Default).launch {

            val apps = getApplication<Application>().applicationContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList
            val filtered = arrayListOf<ApplicationInfo>()

            for (i in apps.indices) {

                if (searchKeywords.value.isNullOrEmpty()) {
                    appData.postValue(filtered)
                    return@launch
                }

                apps[i].name = getApplicationName(getApplication<Application>().applicationContext, apps[i])

                if (apps[i].name.contains(searchKeywords.value!!, true) || apps[i].packageName.contains(searchKeywords.value!!, true)) {
                    when (MainPreferences.getListAppCategory()) {
                        AppCategoryPopup.SYSTEM -> {
                            if ((apps[i].flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                                filtered.add(apps[i])
                            }
                        }
                        AppCategoryPopup.USER -> {
                            if ((apps[i].flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                                filtered.add(apps[i])
                            }
                        }
                        else -> {
                            filtered.add(apps[i])
                        }
                    }
                }
            }

            filtered.getSortedList(MainPreferences.getSortStyle(), getApplication<Application>().applicationContext)

            appData.postValue(filtered)
        }
    }
}
