package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.util.PackageUtils.getApplicationName
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppData(application: Application)
    : AndroidViewModel(application), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private val appData: MutableLiveData<ArrayList<ApplicationInfo>> by lazy {
        MutableLiveData<ArrayList<ApplicationInfo>>().also {
            loadAppData()
        }
    }

    fun getAppData(): LiveData<ArrayList<ApplicationInfo>> {
        return appData
    }

    fun loadAppData() {
        launch {

            val apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList

            val filtered = arrayListOf<ApplicationInfo>()

            for (i in apps.indices) {
                /**
                 * [ApplicationInfo.name] is pretty useless anyway, so here
                 * I am making it more meaningful and usable and this also
                 * saves time from creating a new data model and refactoring whole
                 * project
                 */
                apps[i].name = getApplicationName(getApplication<Application>().applicationContext, apps[i])

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

            filtered.getSortedList(MainPreferences.getSortStyle(), getApplication<Application>().applicationContext)

            appData.postValue(filtered)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
