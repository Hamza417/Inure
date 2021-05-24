package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
