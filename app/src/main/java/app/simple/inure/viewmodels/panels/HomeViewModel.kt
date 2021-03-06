package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.model.PackageStats
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private var usageStatsManager: UsageStatsManager = getApplication<Application>()
            .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

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

    val frequentlyUsed: MutableLiveData<ArrayList<PackageStats>> by lazy {
        MutableLiveData<ArrayList<PackageStats>>().also {
            loadFrequentlyUsed()
        }
    }

    private fun loadFrequentlyUsed() {
        viewModelScope.launch(Dispatchers.Default) {
            val stats = with(UsageInterval.getTimeInterval()) {
                usageStatsManager.queryAndAggregateUsageStats(first, second)
            }

            val apps = getApplication<Application>()
                    .packageManager.getInstalledPackages(PackageManager.GET_META_DATA)


            val list = arrayListOf<PackageStats>()

            for (app in apps) {
                kotlin.runCatching {
                    val packageStats = PackageStats()

                    packageStats.packageInfo = app

                    packageStats.packageInfo!!.applicationInfo.apply {
                        name = getApplication<Application>().packageManager.getApplicationLabel(this).toString()
                    }

                    packageStats.totalTimeUsed += stats[app.packageName]?.totalTimeInForeground ?: 0

                    list.add(packageStats)
                }.getOrElse {
                    it.printStackTrace()
                }
            }

            list.sortByDescending {
                it.totalTimeUsed
            }

            frequentlyUsed.postValue(list)
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
            val apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

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
            val apps = getApplication<Application>()
                    .applicationContext.packageManager
                    .getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

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
                Pair(R.drawable.ic_smartphone, context.getString(R.string.device_stats))
            )

            menuItems.postValue(list)
        }
    }
}