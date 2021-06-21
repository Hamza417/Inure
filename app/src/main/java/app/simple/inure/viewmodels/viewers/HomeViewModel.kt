package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val recentlyInstalledAppData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadRecentlyInstalledAppData()
        }
    }

    private val recentlyUpdatedAppData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadRecentlyUpdatedAppData()
        }
    }

    private val menuItems: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadItems()
        }
    }

    fun getRecentApps(): LiveData<ArrayList<PackageInfo>> {
        return recentlyInstalledAppData
    }

    fun getUpdatedApps(): LiveData<ArrayList<PackageInfo>> {
        return recentlyUpdatedAppData
    }

    fun getMenuItems(): LiveData<List<Pair<Int, String>>> {
        return menuItems
    }

    private fun loadRecentlyInstalledAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

            apps = apps.stream().filter { p ->
                p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name = PackageUtils.getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.sortByDescending {
                it.firstInstallTime
            }

            recentlyInstalledAppData.postValue(apps)
        }
    }

    private fun loadRecentlyUpdatedAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

            apps = apps.stream().filter { p ->
                p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.sortByDescending {
                it.lastUpdateTime
            }

            recentlyUpdatedAppData.postValue(apps)
        }
    }

    private fun loadItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext

            val list = listOf(
                Pair(R.drawable.ic_apps, context.getString(R.string.apps)),
                Pair(R.drawable.ic_terminal, context.getString(R.string.terminal)),
                Pair(R.drawable.ic_analytics, context.getString(R.string.analytics)),
                Pair(R.drawable.ic_stats, context.getString(R.string.usage_statistics)),
            )

            menuItems.postValue(list)
        }
    }
}