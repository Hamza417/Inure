package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class HomeViewModel(application: Application) : WrappedViewModel(application) {

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

    private val uninstalled: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loadDeletedApps()
            }
        }
    }

    private val disabled: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loadDisabledApps()
            }
        }
    }

    private val menuItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadItems()
        }
    }

    fun getRecentApps(): LiveData<ArrayList<PackageInfo>> {
        return recentlyInstalledAppData
    }

    fun getUpdatedApps(): LiveData<ArrayList<PackageInfo>> {
        return recentlyUpdatedAppData
    }

    fun getUninstalledPackages(): LiveData<ArrayList<PackageInfo>> {
        return uninstalled
    }

    fun getDisabledApps(): LiveData<ArrayList<PackageInfo>> {
        return disabled
    }

    fun getMenuItems(): LiveData<List<Pair<Int, Int>>> {
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadDeletedApps() {
        viewModelScope.launch(Dispatchers.Default) {
            val packageManager = packageManager

            var apps = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES) as ArrayList

            apps = apps.stream().filter { p ->
                !PackageUtils.isPackageInstalled(p.packageName, packageManager)
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.sortBy {
                it.applicationInfo.name
            }

            uninstalled.postValue(apps)
        }
    }

    private fun loadDisabledApps() {
        viewModelScope.launch(Dispatchers.Default) {
            val packageManager = packageManager

            var apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

            apps = apps.stream().filter { p ->
                !p.applicationInfo.enabled
            }.collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.sortBy {
                it.applicationInfo.name
            }

            disabled.postValue(apps)
        }
    }

    private fun loadItems() {
        viewModelScope.launch(Dispatchers.Default) {

            val list = arrayListOf(
                    Pair(R.drawable.ic_app_icon, R.string.apps),
                    Pair(R.drawable.ic_terminal, R.string.terminal),
                    Pair(R.drawable.ic_stats, R.string.usage_statistics),
                    // Pair(R.drawable.ic_memory, R.string.device_info),
                    Pair(0, 0), // Divider
                    Pair(R.drawable.ic_sensors, R.string.sensors),
                    Pair(R.drawable.ic_layers, R.string.batch),
                    Pair(R.drawable.ic_analytics, R.string.analytics),
                    Pair(R.drawable.ic_notes, R.string.notes),
                    // Pair(R.drawable.ic_music_note, R.string.music))
                    Pair(0, 0), // Divider
                    Pair(R.drawable.ic_apps_category_recently_installed, R.string.recently_installed),
                    Pair(R.drawable.ic_apps_category_recently_updated, R.string.recently_updated),
                    Pair(R.drawable.ic_apps_category_most_used, R.string.most_used),
                    Pair(R.drawable.ic_apps_category_deleted_apps, R.string.uninstalled),
                    Pair(R.drawable.ic_apps_category_disabled, R.string.disabled),
                    Pair(0, 0), // Divider
            )

            if (BuildConfig.DEBUG || DevelopmentPreferences.isDebugStateEnabled()) {
                list.add(Pair(R.drawable.ic_music_note, R.string.music))
            }

            menuItems.postValue(list)
        }
    }

    fun refresh() {
        loadRecentlyInstalledAppData()
        loadFrequentlyUsed()
        loadRecentlyUpdatedAppData()
    }
}