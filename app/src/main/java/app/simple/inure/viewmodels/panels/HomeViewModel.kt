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
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.HomeCustomizationModel
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.inure.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class HomeViewModel(application: Application) : PackageUtilsViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    @Suppress("PrivatePropertyName")
    private val PRIVATE_FLAG_HIDDEN = 1 shl 0

    init {
        registerSharedPreferenceChangeListener()
    }

    private val oneMonth = 2592000000 // 30 days

    private val recentlyInstalledAppData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadPackageData()
        }
    }

    private val recentlyUpdatedAppData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadPackageData()
        }
    }

    private val mostUsedAppData: MutableLiveData<ArrayList<PackageStats>> by lazy {
        MutableLiveData<ArrayList<PackageStats>>().also {
            loadPackageData()
        }
    }

    private val uninstalled: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                loadUninstalledApps()
            }
        }
    }

    private val disabled: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadPackageData()
        }
    }

    private val hidden: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadPackageData()
        }
    }

    private val menuItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadItems()
        }
    }

    private val customizableMenuItems: MutableLiveData<ArrayList<HomeCustomizationModel>> by lazy {
        MutableLiveData<ArrayList<HomeCustomizationModel>>().also {
            loadCustomizableMenu()
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

    fun getHiddenApps(): LiveData<ArrayList<PackageInfo>> {
        return hidden
    }

    fun getMenuItems(): LiveData<List<Pair<Int, Int>>> {
        return menuItems
    }

    fun getCustomizableMenuItems(): LiveData<ArrayList<HomeCustomizationModel>> {
        return customizableMenuItems
    }

    private fun loadRecentlyInstalledAppData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getInstalledApps().stream()
                .filter { it.firstInstallTime > System.currentTimeMillis() - oneMonth }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name = PackageUtils.getApplicationName(
                        application.applicationContext, apps[i].applicationInfo)
            }

            apps.sortByDescending {
                it.firstInstallTime
            }

            recentlyInstalledAppData.postValue(apps)
        }
    }

    private fun loadRecentlyUpdatedAppData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getInstalledApps().stream()
                .filter { it.lastUpdateTime > System.currentTimeMillis() - oneMonth && it.firstInstallTime < it.lastUpdateTime }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(application.applicationContext, apps[i].applicationInfo)
            }

            apps.sortByDescending {
                it.lastUpdateTime
            }

            recentlyUpdatedAppData.postValue(apps)
        }
    }

    private fun loadMostUsed() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = with(UsageInterval.getTimeInterval()) {
                (application.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager)
                    .queryAndAggregateUsageStats(startTime, endTime)
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
                        name = application.packageManager.getApplicationLabel(this).toString()
                    }

                    packageStats.totalTimeUsed += stats[app.packageName]?.totalTimeInForeground ?: 0

                    list.add(packageStats)
                }.getOrElse {
                    // The app or data proly got deleted
                    // Move to next app
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
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getUninstalledApps().stream()
                .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0 }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(application.applicationContext, apps[i].applicationInfo)
            }

            apps.sortBy {
                it.applicationInfo.name
            }

            uninstalled.postValue(apps)
        }
    }

    private fun loadDisabledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getInstalledApps().stream()
                .filter { it.applicationInfo.enabled.invert() }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(application.applicationContext, apps[i].applicationInfo)
            }

            apps.sortBy {
                it.applicationInfo.name
            }

            disabled.postValue(apps)
        }
    }

    private fun loadHiddenApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getInstalledApps().stream()
                .filter { it.applicationInfo.flags and PRIVATE_FLAG_HIDDEN == 0 }
                .collect(Collectors.toList()) as ArrayList<PackageInfo>

            for (i in apps.indices) {
                apps[i].applicationInfo.name =
                    PackageUtils.getApplicationName(application.applicationContext, apps[i].applicationInfo)
            }

            apps.sortBy {
                it.applicationInfo.name
            }

            hidden.postValue(apps)
        }
    }

    private fun loadItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = arrayListOf<Pair<Int, Int>>()

            list.add(Pair(R.drawable.ic_app_icon, R.string.apps))

            if (HomePreferences.isPanelVisible(HomePreferences.isTerminalVisible)) {
                list.add(Pair(R.drawable.ic_terminal, R.string.terminal))
            }

            if (HomePreferences.isPanelVisible(HomePreferences.isUsageStatisticsVisible)) {
                list.add(Pair(R.drawable.ic_stats, R.string.usage_statistics))
            }

            list.add(Pair(0, 0)) // Divider

            list.add(Pair(R.drawable.ic_layers, R.string.batch))

            if (HomePreferences.isPanelVisible(HomePreferences.isAnalyticsVisible)) {
                list.add(Pair(R.drawable.ic_analytics, R.string.analytics))
            }

            list.add(Pair(R.drawable.ic_notes, R.string.notes))

            if (DevelopmentPreferences.get(DevelopmentPreferences.enableDeviceInfo)) {
                list.add(Pair(R.drawable.ic_memory, R.string.device_info))
            }

            if (HomePreferences.isPanelVisible(HomePreferences.isSavedCommandsVisible)) {
                list.add(Pair(R.drawable.ic_terminal_saved, R.string.saved_commands))
            }

            list.add(Pair(0, 0)) // Divider
            list.add(Pair(R.drawable.ic_apps_category_recently_installed, R.string.recently_installed))
            list.add(Pair(R.drawable.ic_apps_category_recently_updated, R.string.recently_updated))

            if (HomePreferences.isPanelVisible(HomePreferences.isMostUsedVisible)) {
                list.add(Pair(R.drawable.ic_apps_category_most_used, R.string.most_used))
            }

            if (HomePreferences.isPanelVisible(HomePreferences.isUninstalledVisible)) {
                list.add(Pair(R.drawable.ic_apps_category_deleted_apps, R.string.uninstalled))
            }

            if (HomePreferences.isPanelVisible(HomePreferences.isDisabledVisible)) {
                list.add(Pair(R.drawable.ic_disable, R.string.disabled))
            }

            if (DevelopmentPreferences.get(DevelopmentPreferences.enableHiddenApps)) {
                list.add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
            }

            list.add(Pair(0, 0)) // Divider
            if (HomePreferences.isPanelVisible(HomePreferences.isStackTracesVisible)) {
                list.add(Pair(R.drawable.ic_stacktrace, R.string.stacktraces))
            }

            if (ConfigurationPreferences.isUsingRoot() || ConfigurationPreferences.isUsingShizuku()) {
                if (HomePreferences.isPanelVisible(HomePreferences.isBatteryOptimizationVisible)) {
                    list.add(Pair(R.drawable.ic_settings_power, R.string.battery_optimization))
                }
            }

            if (ConfigurationPreferences.isUsingRoot()) {
                if (HomePreferences.isPanelVisible(HomePreferences.isBootManagerVisible)) {
                    list.add(Pair(R.drawable.ic_power_off, R.string.boot_manager))
                }
            }

            if (HomePreferences.isPanelVisible(HomePreferences.isAPKsVisible)) {
                list.add(Pair(R.drawable.ic_android, R.string.APKs))
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

    private fun loadCustomizableMenu() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = arrayListOf<HomeCustomizationModel>()

            list.add(HomeCustomizationModel(R.string.terminal, R.drawable.ic_terminal, HomePreferences.isTerminalVisible))
            list.add(HomeCustomizationModel(R.string.usage_statistics, R.drawable.ic_stats, HomePreferences.isUsageStatisticsVisible))
            list.add(HomeCustomizationModel(R.string.analytics, R.drawable.ic_analytics, HomePreferences.isAnalyticsVisible))
            list.add(HomeCustomizationModel(R.string.most_used, R.drawable.ic_apps_category_most_used, HomePreferences.isMostUsedVisible))
            list.add(HomeCustomizationModel(R.string.uninstalled, R.drawable.ic_apps_category_deleted_apps, HomePreferences.isUninstalledVisible))
            list.add(HomeCustomizationModel(R.string.disabled, R.drawable.ic_disable, HomePreferences.isDisabledVisible))
            list.add(HomeCustomizationModel(R.string.stacktraces, R.drawable.ic_stacktrace, HomePreferences.isStackTracesVisible))
            list.add(HomeCustomizationModel(R.string.saved_commands, R.drawable.ic_terminal_saved, HomePreferences.isSavedCommandsVisible))
            list.add(HomeCustomizationModel(R.string.battery_optimization, R.drawable.ic_settings_power, HomePreferences.isBatteryOptimizationVisible))
            list.add(HomeCustomizationModel(R.string.boot_manager, R.drawable.ic_power_off, HomePreferences.isBootManagerVisible))
            list.add(HomeCustomizationModel(R.string.APKs, R.drawable.ic_android, HomePreferences.isAPKsVisible))

            customizableMenuItems.postValue(list)
        }
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        refresh()
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        loadRecentlyInstalledAppData()
        loadMostUsed()
        loadRecentlyUpdatedAppData()
        loadDisabledApps()
        loadHiddenApps()
    }

    override fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        loadDeletedApps()
    }

    fun refresh() {
        loadRecentlyInstalledAppData()
        loadMostUsed()
        loadRecentlyUpdatedAppData()
        loadDisabledApps()
        loadHiddenApps()
        loadDeletedApps()
    }

    fun refreshMostUsed() {
        loadMostUsed()
    }

    fun refreshMenuItems() {
        loadItems()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            DevelopmentPreferences.music,
            DevelopmentPreferences.enableDeviceInfo,
            ConfigurationPreferences.isUsingRoot,
            ConfigurationPreferences.isUsingShizuku,
            DevelopmentPreferences.enableHiddenApps,
            HomePreferences.isTerminalVisible,
            HomePreferences.isUsageStatisticsVisible,
            HomePreferences.isAnalyticsVisible,
            HomePreferences.isMostUsedVisible,
            HomePreferences.isUninstalledVisible,
            HomePreferences.isDisabledVisible,
            HomePreferences.isStackTracesVisible,
            HomePreferences.isSavedCommandsVisible,
            HomePreferences.isBatteryOptimizationVisible,
            HomePreferences.isBootManagerVisible
            -> {
                loadItems()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSharedPreferenceChangeListener()
    }
}