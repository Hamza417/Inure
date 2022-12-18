package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.inure.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class HomeViewModel(application: Application) : PackageUtilsViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        registerSharedPreferenceChangeListener()
    }

    private val oneMonth = 2592000000 // 30 days

    private var usageStatsManager: UsageStatsManager = getApplication<Application>()
        .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val recentlyInstalledAppData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    private val recentlyUpdatedAppData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    private val mostUsedAppData: MutableLiveData<ArrayList<PackageStats>> by lazy {
        MutableLiveData<ArrayList<PackageStats>>()
    }

    private val uninstalled: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    private val disabled: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    private val menuItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadItems()
        }
    }

    fun getRecentlyInstalled(): LiveData<ArrayList<PackageInfo>> {
        return recentlyInstalledAppData
    }

    fun getRecentlyUpdated(): LiveData<ArrayList<PackageInfo>> {
        return recentlyUpdatedAppData
    }

    fun getMostUsed(): LiveData<ArrayList<PackageStats>> {
        return mostUsedAppData
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
            val apps = getInstalledApps().stream()
                .filter { it.firstInstallTime > System.currentTimeMillis() - oneMonth }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

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
            val apps = getInstalledApps().stream()
                .filter { it.lastUpdateTime > System.currentTimeMillis() - oneMonth }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

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

    private fun loadMostUsed() {
        viewModelScope.launch(Dispatchers.Default) {
            val stats = with(UsageInterval.getTimeInterval()) {
                usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            }

            val apps = getInstalledApps().stream()
                .filter { stats.containsKey(it.packageName) }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

            var list = arrayListOf<PackageStats>()

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

            list = list
                .filter { it.totalTimeUsed > 0L }
                .sortedByDescending { it.totalTimeUsed }
                .toCollection(ArrayList())

            mostUsedAppData.postValue(list)
        }
    }

    private fun loadDeletedApps() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = getUninstalledApps().stream()
                .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0 }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

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
            val apps = getInstalledApps().stream()
                .filter { it.applicationInfo.enabled.invert() }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

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

            val list = arrayListOf<Pair<Int, Int>>()

            list.add(Pair(R.drawable.ic_app_icon, R.string.apps))
            list.add(Pair(R.drawable.ic_terminal, R.string.terminal))
            list.add(Pair(R.drawable.ic_stats, R.string.usage_statistics))
            // list.add(Pair(R.drawable.ic_memory, R.string.device_info))
            list.add(Pair(0, 0)) // Divider
            list.add(Pair(R.drawable.ic_sensors, R.string.sensors))
            list.add(Pair(R.drawable.ic_layers, R.string.batch))
            list.add(Pair(R.drawable.ic_analytics, R.string.analytics))
            list.add(Pair(R.drawable.ic_notes, R.string.notes))
            // list.add(Pair(R.drawable.ic_music_note, R.string.music))
            list.add(Pair(0, 0)) // Divider
            list.add(Pair(R.drawable.ic_apps_category_recently_installed, R.string.recently_installed))
            list.add(Pair(R.drawable.ic_apps_category_recently_updated, R.string.recently_updated))
            list.add(Pair(R.drawable.ic_apps_category_most_used, R.string.most_used))
            list.add(Pair(R.drawable.ic_apps_category_deleted_apps, R.string.uninstalled))
            list.add(Pair(R.drawable.ic_apps_category_disabled, R.string.disabled))

            list.add(Pair(0, 0)) // Divider
            list.add(Pair(R.drawable.ic_stacktrace, R.string.stacktraces))

            if (ConfigurationPreferences.isUsingRoot()) {
                list.add(Pair(R.drawable.ic_settings_power, R.string.battery_optimization))
                list.add(Pair(R.drawable.ic_power_off, R.string.boot_manager))
            }

            if (DevelopmentPreferences.get(DevelopmentPreferences.music)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.add(Pair(R.drawable.ic_music_note, R.string.music))
                }
            }

            // Add a last divider
            list.add(Pair(0, 0))

            menuItems.postValue(list)
        }
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        refresh()
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadRecentlyInstalledAppData()
        loadMostUsed()
        loadRecentlyUpdatedAppData()
        loadDisabledApps()
    }

    override fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        super.onUninstalledAppsLoaded(uninstalledApps)
        loadDeletedApps()
    }

    fun refresh() {
        loadRecentlyInstalledAppData()
        loadMostUsed()
        loadRecentlyUpdatedAppData()
        loadDisabledApps()
        loadDeletedApps()
    }

    fun refreshMostUsed() {
        loadMostUsed()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            DevelopmentPreferences.music,
            ConfigurationPreferences.isUsingRoot -> {
                loadItems()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSharedPreferenceChangeListener()
    }
}