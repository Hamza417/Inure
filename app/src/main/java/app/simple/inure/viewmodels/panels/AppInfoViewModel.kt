package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInfo
import app.simple.inure.apk.utils.PackageUtils.isAppHidden
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.isSystemApp
import app.simple.inure.apk.utils.PackageUtils.isUpdateInstalled
import app.simple.inure.apk.utils.PackageUtils.isUserApp
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.AppUtils
import app.simple.inure.util.AppUtils.isUnlocker
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.TrackerUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInfoViewModel(application: Application, private var packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val menuItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadMetaOptions()
        }
    }

    private val menuOptions: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadActionOptions()
        }
    }

    private val miscellaneousItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadMiscellaneousItems()
        }
    }

    private val trackers: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            loadTrackers()
        }
    }

    private val tags: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            loadTags()
        }
    }

    private val batteryOptimization: MutableLiveData<BatteryOptimizationModel> by lazy {
        MutableLiveData<BatteryOptimizationModel>().also {
            if (ConfigurationPreferences.isRootOrShizuku()) {
                loadBatteryOptimization()
            }
        }
    }

    fun getComponentsOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuItems
    }

    fun getActionsOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuOptions
    }

    fun getMiscellaneousItems(): LiveData<List<Pair<Int, Int>>> {
        return miscellaneousItems
    }

    fun getTrackers(): LiveData<Int> {
        return trackers
    }

    fun getTags(): LiveData<ArrayList<String>> {
        return tags
    }

    fun getBatteryOptimization(): LiveData<BatteryOptimizationModel> {
        return batteryOptimization
    }

    fun loadActionOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                warning.postValue(context.getString(R.string.app_not_installed, packageInfo.packageName))

                if (ConfigurationPreferences.isUsingRoot()) {
                    list.rootMenu()
                } else if (ConfigurationPreferences.isUsingShizuku()) {
                    list.shizukuMenu()
                } else {
                    list.normalMenu()
                }
            } else {
                if (packageInfo.applicationInfo.sourceDir.toFile().exists()) {
                    list.add(Pair(R.drawable.ic_send, R.string.send))

                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0) {
                        list.add(Pair(R.drawable.ic_publish, R.string.install))
                    } else {
                        if (ConfigurationPreferences.isUsingShizuku() || ConfigurationPreferences.isUsingRoot()) {
                            list.add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
                        }
                    }
                }
            }

            if (isNotThisApp().invert()) {
                list.add(Pair(R.drawable.ic_change_history, R.string.change_logs))
                list.add(Pair(R.drawable.ic_credits, R.string.credits))
                list.add(Pair(R.drawable.ic_translate, R.string.translate))
            }

            // Check if app has settings activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.queryIntentActivities(Intent().apply {
                    action = Intent.ACTION_APPLICATION_PREFERENCES
                }, 0).let { resolveInfos ->
                    resolveInfos.forEach {
                        if (it.activityInfo.packageName == packageInfo.packageName) {
                            if (it.activityInfo.exported) {
                                list.add(Pair(R.drawable.ic_settings, R.string.preferences))
                            }
                        }
                    }
                }
            }

            if (packageInfo.isInstalled()) {
                packageManager.queryIntentActivities(Intent().apply {
                    action = Intent.ACTION_SEARCH
                }, 0).let { resolveInfos ->
                    for (resolveInfo in resolveInfos) {
                        if (resolveInfo.activityInfo.packageName == packageInfo.packageName) {
                            if (resolveInfo.activityInfo.exported) {
                                list.add(Pair(R.drawable.ic_search, R.string.search))
                            }
                            break
                        }
                    }
                }
            }

            if (packageInfo.isInstalled()) {
                packageInfo.applicationInfo.manageSpaceActivityName?.let { it ->
                    try {
                        packageManager.getActivityInfo(ComponentName(packageInfo.packageName, it), 0).let {
                            if (it.exported) {
                                list.add(Pair(R.drawable.ic_sd_storage, R.string.manage_space))
                            }
                        }
                    } catch (e: NameNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }

            menuOptions.postValue(list)
        }
    }

    fun loadMetaOptions() {
        viewModelScope.launch(Dispatchers.Default) {

            val isInstalled = packageManager.isPackageInstalled(packageInfo.packageName)

            val list = mutableListOf<Pair<Int, Int>>()

            list.add(Pair(R.drawable.ic_permission, R.string.permissions))
            list.add(Pair(R.drawable.ic_activities, R.string.activities))
            list.add(Pair(R.drawable.ic_services, R.string.services))
            list.add(Pair(R.drawable.ic_certificate, R.string.certificate))
            list.add(Pair(R.drawable.ic_resources, R.string.resources))
            list.add(Pair(R.drawable.ic_receivers, R.string.receivers))
            list.add(Pair(R.drawable.ic_provider, R.string.providers))
            list.add(Pair(R.drawable.ic_android, R.string.manifest))
            list.add(Pair(R.drawable.ic_anchor, R.string.uses_feature))
            list.add(Pair(R.drawable.ic_graphics, R.string.graphics))
            list.add(Pair(R.drawable.ic_extras, R.string.extras))
            list.add(Pair(R.drawable.ic_shared_libs, R.string.shared_libs))
            if (isInstalled) {
                if (packageInfo.packageName != "android") { // Android System dex classes are not supported
                    list.add(Pair(R.drawable.ic_code, R.string.dex_classes))
                }
            }
            list.add(Pair(R.drawable.ic_radiation_nuclear, R.string.trackers))

            if (isInstalled) {
                if (ConfigurationPreferences.isUsingRoot()) {
                    list.add(Pair(R.drawable.ic_power_off, R.string.boot))
                }
            }

            if (ConfigurationPreferences.isRootOrShizuku() && isInstalled) {
                if (isNotThisApp()) {
                    list.add(1, Pair(R.drawable.ic_rocket_launch, R.string.operations))
                }
            }

            if (ConfigurationPreferences.isUsingRoot()) {
                if (isNotThisApp()) {
                    list.add(Pair(R.drawable.ic_settings, R.string.shared_prefs))
                }
            }

            menuItems.postValue(list)
        }
    }

    fun loadMiscellaneousItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            if (packageInfo.isUnlocker().invert()) {
                list.add(Pair(R.drawable.ic_downloading, R.string.extract))
            }

            list.add(Pair(R.drawable.ic_play_store, R.string.play_store))

            if (AppUtils.isGithubFlavor() || AppUtils.isBetaFlavor()) {
                if (packageInfo.isUnlocker().invert()) {
                    list.add(Pair(R.drawable.ic_fdroid, R.string.fdroid))
                }
            }

            miscellaneousItems.postValue(list)
        }
    }

    private fun ArrayList<Pair<Int, Int>>.rootMenu() {
        if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
            add(Pair(R.drawable.ic_launch, R.string.launch))
        }

        if (packageInfo.isUnlocker().invert()) {
            add(Pair(R.drawable.ic_send, R.string.send))
        }

        if (isNotThisApp()) {
            add(Pair(R.drawable.ic_delete, R.string.uninstall))

            if (packageInfo.isUserApp()) {
                add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
            }

            if (packageInfo.isSystemApp()) {
                if (packageInfo.isUpdateInstalled()) {
                    add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                }
            }

            if (packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                add(Pair(R.drawable.ic_disable, R.string.disable))
            } else {
                add(Pair(R.drawable.ic_check, R.string.enable))
            }

            if (DevelopmentPreferences.get(DevelopmentPreferences.ENABLE_HIDDEN_APPS)) {
                if (packageManager.isAppHidden(packageInfo.packageName)) {
                    add(Pair(R.drawable.ic_visibility, R.string.visible))
                } else {
                    add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
                }
            }

            add(Pair(R.drawable.ic_close, R.string.force_stop))
            add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
        }

        add(Pair(R.drawable.ic_broom, R.string.clear_cache))
        add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
    }

    private fun ArrayList<Pair<Int, Int>>.shizukuMenu() {
        if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
            add(Pair(R.drawable.ic_launch, R.string.launch))
        }

        if (packageInfo.isUnlocker().invert()) {
            add(Pair(R.drawable.ic_send, R.string.send))
        }

        if (isNotThisApp()) {
            add(Pair(R.drawable.ic_delete, R.string.uninstall))

            if (packageInfo.isUserApp()) {
                add(Pair(R.drawable.ic_restart_alt, R.string.reinstall))
            }

            if (packageInfo.isSystemApp()) {
                if (packageInfo.isUpdateInstalled()) {
                    add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                }
            }

            if (packageManager.getApplicationInfo(packageInfo.packageName)!!.enabled) {
                add(Pair(R.drawable.ic_disable, R.string.disable))
            } else {
                add(Pair(R.drawable.ic_check, R.string.enable))
            }

            if (DevelopmentPreferences.get(DevelopmentPreferences.ENABLE_HIDDEN_APPS)) {
                if (packageManager.isAppHidden(packageInfo.packageName)) {
                    add(Pair(R.drawable.ic_visibility, R.string.visible))
                } else {
                    add(Pair(R.drawable.ic_visibility_off, R.string.hidden))
                }
            }

            add(Pair(R.drawable.ic_close, R.string.force_stop))
            add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
        }

        add(Pair(R.drawable.ic_broom, R.string.clear_cache))
        add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
    }

    private fun ArrayList<Pair<Int, Int>>.normalMenu() {
        if (packageInfo.isUserApp()) {
            if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName) && isNotThisApp()) {
                add(Pair(R.drawable.ic_launch, R.string.launch))
            }

            if (packageInfo.isUnlocker().invert()) {
                add(Pair(R.drawable.ic_send, R.string.send))
            }

            if (isNotThisApp()) {
                add(Pair(R.drawable.ic_delete, R.string.uninstall))
            }
        } else {
            if (PackageUtils.checkIfAppIsLaunchable(applicationContext(), packageInfo.packageName)) {
                add(Pair(R.drawable.ic_launch, R.string.launch))
            }

            if (packageInfo.isUnlocker().invert()) {
                add(Pair(R.drawable.ic_send, R.string.send))
            }

            if (isNotThisApp()) {
                if (packageInfo.isUpdateInstalled()) {
                    add(Pair(R.drawable.ic_layers_clear, R.string.uninstall_updates))
                }
            }
        }

        add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
    }

    private fun loadTrackers() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val packageInfo: PackageInfo = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.MATCH_DISABLED_COMPONENTS)
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.GET_DISABLED_COMPONENTS)
                    }
                } catch (e: NameNotFoundException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.MATCH_DISABLED_COMPONENTS)!!
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.GET_DISABLED_COMPONENTS)!!
                    }
                }

                val trackers = TrackerUtils.getTrackerSignatures()
                var count = 0

                if (packageInfo.activities != null) {
                    for (activity in packageInfo.activities) {
                        for (tracker in trackers) {
                            if (activity.name.lowercase().contains(tracker.lowercase())) {
                                count++
                                break
                            }
                        }
                    }
                }

                if (packageInfo.services != null) {
                    for (service in packageInfo.services) {
                        for (tracker in trackers) {
                            if (service.name.lowercase().contains(tracker.lowercase())) {
                                count++
                                break
                            }
                        }
                    }
                }

                if (packageInfo.receivers != null) {
                    for (receiver in packageInfo.receivers) {
                        for (tracker in trackers) {
                            if (receiver.name.lowercase().contains(tracker.lowercase())) {
                                count++
                                break
                            }
                        }
                    }
                }

                this@AppInfoViewModel.trackers.postValue(count)
            }.getOrElse {
                this@AppInfoViewModel.trackers.postValue(0)
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsDataBase = TagsDatabase.getInstance(application.applicationContext)
            val tags = tagsDataBase?.getTagDao()?.getTagsByPackage(packageInfo.packageName)?.toArrayList()

            if (tags.isNullOrEmpty().invert()) {
                this@AppInfoViewModel.tags.postValue(tags)
            } else {
                this@AppInfoViewModel.tags.postValue(arrayListOf())
            }
        }
    }

    private fun isNotThisApp(): Boolean {
        return packageInfo.packageName != application.packageName
    }

    fun reinitPackageInfo(): PackageInfo {
        packageInfo = packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA)
        return packageInfo
    }

    private fun loadBatteryOptimization() {
        viewModelScope.launch(Dispatchers.IO) {
            val batteryOptimizationModel = BatteryOptimizationModel()
            batteryOptimizationModel.packageInfo = packageInfo

            runCatching {
                when {
                    ConfigurationPreferences.isUsingRoot() -> {
                        Shell.cmd("dumpsys deviceidle whitelist").exec().let { result ->
                            if (result.isSuccess) {
                                if (result.out.isNotEmpty()) {
                                    val lines = result.out
                                    for (line in lines) {
                                        if (line.contains(packageInfo.packageName)) {
                                            batteryOptimizationModel.isOptimized = false
                                            break
                                        } else {
                                            batteryOptimizationModel.isOptimized = true
                                        }
                                    }
                                } else {
                                    batteryOptimizationModel.isOptimized = true
                                }
                            }
                        }

                        batteryOptimization.postValue(batteryOptimizationModel)
                    }
                    ConfigurationPreferences.isUsingShizuku() -> {
                        ShizukuServiceHelper.getInstance().getBoundService { service ->
                            service.simpleExecute("dumpsys deviceidle whitelist").let { result ->
                                if (result.isSuccess) {
                                    if (result.output?.isNotEmpty() == true) {
                                        val lines = result.output.split("\n")
                                        for (line in lines) {
                                            if (line.contains(packageInfo.packageName)) {
                                                batteryOptimizationModel.isOptimized = false
                                                break
                                            } else {
                                                batteryOptimizationModel.isOptimized = true
                                            }
                                        }
                                    } else {
                                        batteryOptimizationModel.isOptimized = true
                                    }
                                }
                            }

                            batteryOptimization.postValue(batteryOptimizationModel)
                        }
                    }
                }
            }
        }
    }

    fun setBatteryOptimization(packageInfo: PackageInfo, optimize: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                ConfigurationPreferences.isUsingRoot() -> {
                    if (optimize) {
                        Shell.cmd("cmd deviceidle whitelist -${packageInfo.packageName}").exec().let {
                            if (it.isSuccess) {
                                batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, true))
                            } else {
                                batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, false))
                            }
                        }
                    } else {
                        Shell.cmd("cmd deviceidle whitelist +${packageInfo.packageName}").exec().let {
                            if (it.isSuccess) {
                                batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, false))
                            } else {
                                batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, true))
                            }
                        }
                    }
                }
                ConfigurationPreferences.isUsingShizuku() -> {
                    if (optimize) {
                        ShizukuServiceHelper.getInstance().getBoundService { service ->
                            service.simpleExecute("cmd deviceidle whitelist -${packageInfo.packageName}").let { result ->
                                if (result.isSuccess) {
                                    batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, true))
                                } else {
                                    batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, false))
                                }
                            }
                        }
                    } else {
                        ShizukuServiceHelper.getInstance().getBoundService { service ->
                            service.simpleExecute("cmd deviceidle whitelist +${packageInfo.packageName}").let { result ->
                                if (result.isSuccess) {
                                    batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, false))
                                } else {
                                    batteryOptimization.postValue(BatteryOptimizationModel(packageInfo, true))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
