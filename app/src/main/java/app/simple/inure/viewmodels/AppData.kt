package app.simple.inure.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import app.simple.inure.packagehelper.PackageUtils.getApplicationName
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppData(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val appData: MutableLiveData<ArrayList<ApplicationInfo>> by lazy {
        MutableLiveData<ArrayList<ApplicationInfo>>().also {
            loadAppData()
        }
    }

    fun getAppData(): LiveData<ArrayList<ApplicationInfo>> {
        return appData
    }

    fun loadAppData() {
        CoroutineScope(Dispatchers.Default).launch {
            val apps = getApplication<Application>().applicationContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList
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
