package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.exception.CacheDirectoryDeletionException
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.stream.Collectors

class AllAppsData(application: Application) : AndroidViewModel(application) {

    private val appData: MutableLiveData<ArrayList<ApplicationInfo>> by lazy {
        MutableLiveData<ArrayList<ApplicationInfo>>().also {
            loadAppData()
        }
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

            kotlin.runCatching {
                if (File(getApplication<Application>().getExternalFilesDir(null)?.path + "/send_cache/").deleteRecursively()) {
                    Log.d(getApplication<Application>().packageName, "Deleted")
                } else {
                    throw CacheDirectoryDeletionException("Could not delete cache directory, manual action required.")
                }
            }.getOrElse {
                it.printStackTrace()
            }
        }
    }
}
