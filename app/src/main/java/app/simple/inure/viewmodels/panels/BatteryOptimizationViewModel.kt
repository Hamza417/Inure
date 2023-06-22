package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.SortConstant
import app.simple.inure.constants.Warnings
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.SortBatteryOptimization.getSortedList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class BatteryOptimizationViewModel(application: Application) : RootShizukuViewModel(application) {

    init {
        initializeCoreFramework()
    }

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
            var apps = getInstalledPackages()

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
                            }
                        }

                        var filtered = arrayListOf<BatteryOptimizationModel>()

                        if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.OPTIMIZED)) {
                            filtered = batteryOptimizationArrayList.stream().filter {
                                it.isOptimized
                            }.collect(Collectors.toList()) as ArrayList<BatteryOptimizationModel>
                        }

                        if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.NOT_OPTIMIZED)) {
                            filtered = batteryOptimizationArrayList.stream().filter {
                                it.isOptimized.invert()
                            }.collect(Collectors.toList()) as ArrayList<BatteryOptimizationModel>
                        }

                        for (app in filtered) {
                            kotlin.runCatching {
                                app.packageInfo.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageInfo.packageName)
                            }
                        }

                        filtered.getSortedList()
                        batteryOptimizationData.postValue(filtered)
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun loadBatteryOptimizationShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            var apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

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
                ShizukuUtils.execInternal(Command("dumpsys deviceidle whitelist"), null).let { result ->
                    Log.d("BatteryOptimizationShizukuViewModel", "loadBatteryOptimizationShizuku: ${result.out}")
                    apps.forEach { packageInfo ->
                        kotlin.runCatching {
                            val outData = result.out.split("\n").find { out ->
                                packageInfo.packageName == out.subSequence(out.indexOf(",").plus(1), out.lastIndexOf(",")).trim()
                            }

                            if (outData.isNotNull()) {
                                val batteryOptimizationModel = BatteryOptimizationModel()

                                val type = outData!!.subSequence(0, endIndex = outData.indexOf(",")).trim()
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
                        }
                    }

                    var filtered = arrayListOf<BatteryOptimizationModel>()

                    if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.OPTIMIZED)) {
                        filtered = batteryOptimizationArrayList.stream().filter {
                            it.isOptimized
                        }.collect(Collectors.toList()) as ArrayList<BatteryOptimizationModel>
                    }

                    if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.NOT_OPTIMIZED)) {
                        filtered = batteryOptimizationArrayList.stream().filter {
                            it.isOptimized.invert()
                        }.collect(Collectors.toList()) as ArrayList<BatteryOptimizationModel>
                    }

                    for (app in filtered) {
                        kotlin.runCatching {
                            app.packageInfo.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageInfo.packageName)
                        }
                    }

                    filtered.getSortedList()
                    batteryOptimizationData.postValue(filtered)
                }
            }.getOrElse {
                postError(it)
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
                        if (it.isSuccessful) {
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
        if (ConfigurationPreferences.isUsingShizuku()) {
            loadBatteryOptimizationShizuku()
        } else if (ConfigurationPreferences.isUsingRoot()) {
            loadBatteryOptimizationSu()
        }
    }

    private fun getInstalledPackages(): MutableList<PackageInfo> {
        while (true) {
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    return packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                }
            }
        }
    }

    fun clearBatteryOptimizationAppData() {
        batteryOptimizationUpdate.value = null
    }

    fun isBatteryOptimizationDataEmpty(): Boolean {
        kotlin.runCatching {
            return batteryOptimizationData.value.isNullOrEmpty()
        }.getOrElse {
            return true
        }
    }

    override fun onShellCreated(shell: Shell?) {
        loadBatteryOptimizationSu()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
        batteryOptimizationData.postValue(ArrayList())
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        refresh()
    }

    override fun onShizukuCreated() {
        loadBatteryOptimizationShizuku()
    }
}
