package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.SortBatteryOptimization.getSortedList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class BatteryOptimizationViewModel(application: Application) : RootShizukuViewModel(application) {

    private val batteryOptimizationArrayList = ArrayList<BatteryOptimizationModel>()

    private val batteryOptimizationData: MutableLiveData<ArrayList<BatteryOptimizationModel>> by lazy {
        MutableLiveData<ArrayList<BatteryOptimizationModel>>()
    }

    private val batteryOptimizationUpdate: MutableLiveData<Pair<BatteryOptimizationModel, Int>> by lazy {
        MutableLiveData<Pair<BatteryOptimizationModel, Int>>()
    }

    fun getBatteryOptimizationData(): LiveData<ArrayList<BatteryOptimizationModel>> {
        return batteryOptimizationData
    }

    fun getBatteryOptimizationUpdate(): LiveData<Pair<BatteryOptimizationModel, Int>> {
        return batteryOptimizationUpdate
    }

    private fun loadBatteryOptimizationSu() {
        viewModelScope.launch(Dispatchers.IO) {
            var apps = getInstalledApps()

            when (BatteryOptimizationPreferences.getApplicationType()) {
                SortConstant.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }

                SortConstant.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            kotlin.runCatching {
                Shell.cmd("dumpsys deviceidle whitelist").exec().let { result ->
                    if (result.isSuccess) {
                        apps.forEach { packageInfo ->
                            kotlin.runCatching {
                                val outData = result.out.find { out ->
                                    packageInfo.packageName == out.subSequence(out.indexOf(",").plus(1), out.lastIndexOf(",")).trim()
                                }

                                if (outData.isNotNull()) {
                                    val batteryOptimizationModel = BatteryOptimizationModel()
                                    val type = outData!!.subSequence(0, endIndex = outData.indexOf(",")).trim()

                                    batteryOptimizationModel.packageInfo = packageInfo
                                    batteryOptimizationModel.type = type.toString()
                                    batteryOptimizationModel.isOptimized = false // App is not optimized
                                    batteryOptimizationArrayList.add(batteryOptimizationModel)
                                } else {
                                    val batteryOptimizationModel = BatteryOptimizationModel()
                                    batteryOptimizationModel.packageInfo = packageInfo
                                    batteryOptimizationModel.isOptimized = true // App is optimized for better battery life
                                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                                        batteryOptimizationModel.type = BatteryOptimizationModel.TYPE_SYSTEM
                                    } else {
                                        batteryOptimizationModel.type = BatteryOptimizationModel.TYPE_USER
                                    }
                                    batteryOptimizationArrayList.add(batteryOptimizationModel)
                                }
                            }
                        }

                        var filtered = arrayListOf<BatteryOptimizationModel>()

                        for (app in batteryOptimizationArrayList) {
                            if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.OPTIMIZED)) {
                                if (app.isOptimized) {
                                    if (!filtered.contains(app)) {
                                        filtered.add(app)
                                    }
                                }
                            }

                            if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.NOT_OPTIMIZED)) {
                                if (!app.isOptimized) {
                                    if (!filtered.contains(app)) {
                                        filtered.add(app)
                                    }
                                }
                            }
                        }

                        filtered = filtered.distinct() as ArrayList<BatteryOptimizationModel>

                        for (app in filtered) {
                            kotlin.runCatching {
                                app.packageInfo.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageInfo.packageName)
                            }
                        }

                        filtered.getSortedList()

                        // Remove duplicates
                        filtered = filtered.distinctBy {
                            it.packageInfo.packageName
                        } as ArrayList<BatteryOptimizationModel>

                        batteryOptimizationData.postValue(filtered)
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun loadBatteryOptimizationShizuku(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            var apps = getInstalledApps()

            when (BatteryOptimizationPreferences.getApplicationType()) {
                SortConstant.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }

                SortConstant.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            kotlin.runCatching {
                shizukuServiceHelper.service!!.execute(arrayListOf("dumpsys", "deviceidle", "whitelist"), null, null).let { result ->
                    apps.forEach { packageInfo ->
                        kotlin.runCatching {
                            val outData = result.output!!.trim().split("\n").find { out ->
                                packageInfo.packageName == out.subSequence(out.indexOf(",").plus(1), out.lastIndexOf(",")).trim()
                            }

                            if (outData != null) {
                                val batteryOptimizationModel = BatteryOptimizationModel()

                                val type = outData.subSequence(0, endIndex = outData.indexOf(",")).trim()
                                // val packageName = outData.subSequence(outData.indexOf(",").plus(1), outData.lastIndexOf(",")).trim()
                                // val uid = outData.subSequence(outData.lastIndexOf(",").plus(1), outData.length).trim()

                                batteryOptimizationModel.packageInfo = packageInfo
                                batteryOptimizationModel.type = type.toString()
                                batteryOptimizationModel.isOptimized = false // App is not optimized
                                batteryOptimizationArrayList.add(batteryOptimizationModel)
                            } else {
                                val batteryOptimizationModel = BatteryOptimizationModel()
                                batteryOptimizationModel.packageInfo = packageInfo
                                batteryOptimizationModel.isOptimized = true // App is optimized for better battery life
                                if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                                    batteryOptimizationModel.type = BatteryOptimizationModel.TYPE_SYSTEM
                                } else {
                                    batteryOptimizationModel.type = BatteryOptimizationModel.TYPE_USER
                                }
                                batteryOptimizationArrayList.add(batteryOptimizationModel)
                            }
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }

                    Log.d("BatteryOptimizationViewModel", "loadBatteryOptimizationShizuku: ${batteryOptimizationArrayList.size}")
                    var filtered = arrayListOf<BatteryOptimizationModel>()

                    for (app in batteryOptimizationArrayList) {
                        if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.OPTIMIZED)) {
                            if (app.isOptimized) {
                                if (!filtered.contains(app)) {
                                    filtered.add(app)
                                }
                            }
                        }

                        if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.NOT_OPTIMIZED)) {
                            if (!app.isOptimized) {
                                if (!filtered.contains(app)) {
                                    filtered.add(app)
                                }
                            }
                        }
                    }

                    // Remove duplicates
                    filtered = filtered.distinctBy {
                        it.packageInfo.packageName
                    } as ArrayList<BatteryOptimizationModel>

                    for (app in filtered) {
                        kotlin.runCatching {
                            app.packageInfo.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageInfo.packageName)
                        }
                    }

                    filtered.getSortedList()

                    batteryOptimizationData.postValue(filtered)
                }
            }.getOrElse {
                batteryOptimizationData.postValue(arrayListOf())

                if (it is ClassCastException) {
                    postWarning("ERR: Shizuku didn't respond properly for battery optimization data fetch request, try restarting Shizuku.")
                } else {
                    postWarning("ERR: ${it.message ?: "Unknown shizuku error while loading battery optimization data"}}")
                }
            }
        }
    }

    fun setBatteryOptimization(batteryOptimizationModel: BatteryOptimizationModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cmd = "dumpsys deviceidle whitelist ${if (batteryOptimizationModel.isOptimized) "+" else "-"}${batteryOptimizationModel.packageInfo.packageName}"

            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd(cmd).exec().let {
                    if (it.isSuccess) {
                        // Check if the command was successful
                        // Invert the state here
                        batteryOptimizationModel.isOptimized = !batteryOptimizationModel.isOptimized
                        batteryOptimizationUpdate.postValue(Pair(batteryOptimizationModel, position))
                    }
                }
            } else if (ConfigurationPreferences.isUsingShizuku()) {
                kotlin.runCatching {
                    ShizukuUtils.execInternal(Command(cmd), null).let {
                        if (it.isSuccess) {
                            // Invert the state here
                            batteryOptimizationModel.isOptimized = !batteryOptimizationModel.isOptimized
                            batteryOptimizationUpdate.postValue(Pair(batteryOptimizationModel, position))
                        }
                    }
                }.onFailure {
                    postError(it)
                }
            }
        }
    }

    fun refresh() {
        batteryOptimizationArrayList.clear()
        initializeCoreFramework()
    }

    fun clearBatteryOptimizationAppData() {
        batteryOptimizationUpdate.value = null
    }

    override fun onShellCreated(shell: Shell?) {
        loadBatteryOptimizationSu()
    }

    override fun onShellDenied() {
        batteryOptimizationData.postValue(ArrayList())
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        refresh()
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        super.onShizukuCreated(shizukuServiceHelper)
        loadBatteryOptimizationShizuku(shizukuServiceHelper)
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        initializeCoreFramework()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ConfigurationPreferences.isUsingRoot,
            ConfigurationPreferences.isUsingShizuku -> {
                refresh()
            }
        }
    }
}
