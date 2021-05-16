package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.util.PackageUtils.getApplicationName
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppsAnalyticsData(application: Application) : AndroidViewModel(application) {

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

            val apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList

            appData.postValue(apps)
        }
    }
}
