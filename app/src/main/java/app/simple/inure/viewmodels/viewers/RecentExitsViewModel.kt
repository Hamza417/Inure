package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.ApplicationExitInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.ActivityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentExitsViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val exitReasons: MutableLiveData<List<ApplicationExitInfo>> by lazy {
        MutableLiveData<List<ApplicationExitInfo>>().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                loadExitReasons()
            }
        }
    }

    fun getExitReasons(): LiveData<List<ApplicationExitInfo>> {
        return exitReasons
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadExitReasons() {
        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                val reasons = ActivityUtils.getRecentExitsList(applicationContext(), packageInfo.packageName)
                exitReasons.postValue(reasons)
            }.getOrElse {
                postError(it)
            }
        }
    }
}