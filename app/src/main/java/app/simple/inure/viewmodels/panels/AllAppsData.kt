package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.events.AppsEvent
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AllAppsData(application: Application) : AndroidViewModel(application) {

    val appData: MutableLiveData<ArrayList<ApplicationInfo>> by lazy {
        MutableLiveData<ArrayList<ApplicationInfo>>().also {
            loadAppData()
        }
    }

    val appLoaded: MutableLiveData<AppsEvent<Boolean>> by lazy {
        MutableLiveData<AppsEvent<Boolean>>()
    }

    fun getAppData(): LiveData<ArrayList<ApplicationInfo>> {
        return appData
    }

    fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList

            when (MainPreferences.getListAppCategory()) {
                AppCategoryPopup.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<ApplicationInfo>
                }
                AppCategoryPopup.USER -> {
                    apps = apps.stream().filter { p ->
                        p.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<ApplicationInfo>
                }
            }

            for (i in apps.indices) {
                apps[i].name = getApplicationName(getApplication<Application>().applicationContext, apps[i])
            }

            apps.getSortedList(MainPreferences.getSortStyle(), getApplication<Application>().applicationContext)

            appData.postValue(apps)
            appLoaded.postValue(AppsEvent(true))
        }
    }
}
