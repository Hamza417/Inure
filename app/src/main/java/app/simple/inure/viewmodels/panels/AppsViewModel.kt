package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.events.AppsEvent
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AppsViewModel(application: Application) : WrappedViewModel(application) {

    private val appData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadAppData()
        }
    }

    val appLoaded: MutableLiveData<AppsEvent<Boolean>> by lazy {
        MutableLiveData<AppsEvent<Boolean>>()
    }

    fun getAppData(): LiveData<ArrayList<PackageInfo>> {
        return appData
    }

    fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

            when (MainPreferences.getAppsCategory()) {
                PopupAppsCategory.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                PopupAppsCategory.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            for (i in apps.indices) {
                apps[i].applicationInfo.name = getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.getSortedList(MainPreferences.getSortStyle(), MainPreferences.isReverseSorting())

            appData.postValue(apps)
            appLoaded.postValue(AppsEvent(true))
        }
    }
}
