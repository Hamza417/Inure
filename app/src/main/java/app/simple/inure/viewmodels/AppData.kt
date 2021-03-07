package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import app.simple.inure.util.PackageUtils.getApplicationName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppData(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val appData: MutableLiveData<MutableList<ApplicationInfo>> by lazy {
        MutableLiveData<MutableList<ApplicationInfo>>().also {
            loadAppData()
        }
    }

    fun getAppData(): LiveData<MutableList<ApplicationInfo>> {
        return appData
    }

    private fun loadAppData() {
        CoroutineScope(Dispatchers.Default).launch {
            val apps = getApplication<Application>().applicationContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            apps.sortBy {
                getApplicationName(getApplication<Application>().applicationContext, it)
            }
            appData.postValue(apps)
        }
    }

    /**
     * Scroll position of the recycler view before the view
     * is destroyed during configuration change
     * @return [LiveData] int type can be null/negative
     */
    private fun getScrollPosition(): LiveData<Int> {
        return savedStateHandle.getLiveData("scroll_position")
    }

    @Deprecated("Use adapter state restoration policy")
    /**
     * Set current scroll position of recycler view
     * right before a view is deemed destroyed
     *
     * use [getScrollPosition] to fetch [LiveData]
     * containing scroll position
     */
    fun setScrollPosition(position: Int) {
        savedStateHandle.set("scroll_position", position)
    }
}
