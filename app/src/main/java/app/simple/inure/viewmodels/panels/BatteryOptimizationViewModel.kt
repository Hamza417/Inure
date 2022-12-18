package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.RootViewModel
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.ui.panels.BatteryOptimization
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.SortBatteryOptimization.getSortedList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class BatteryOptimizationViewModel(application: Application) : RootViewModel(application) {

    init {
        initShell()
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

    private fun loadBatteryOptimization() {
        viewModelScope.launch(Dispatchers.IO) {
            var apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

            when (BatteryOptimizationPreferences.getBatteryOptimizationCategory()) {
                PopupAppsCategory.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                PopupAppsCategory.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            kotlin.runCatching {
                Shell.cmd("dumpsys deviceidle whitelist").exec().let { result ->
                    if (result.isSuccess) {
                        apps.forEach { packageInfo ->
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

                        for (app in batteryOptimizationArrayList) {
                            kotlin.runCatching {
                                app.packageInfo.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageInfo.packageName)
                            }
                        }

                        batteryOptimizationArrayList.getSortedList()
                        batteryOptimizationData.postValue(batteryOptimizationArrayList)
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    fun setBatteryOptimization(batteryOptimizationModel: BatteryOptimizationModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Shell.cmd("dumpsys deviceidle whitelist ${if (batteryOptimizationModel.isOptimized) "+" else "-"}${batteryOptimizationModel.packageInfo.packageName}").exec().let {
                if (it.isSuccess) {
                    batteryOptimizationModel.isOptimized = !batteryOptimizationModel.isOptimized
                    batteryOptimizationUpdate.postValue(Pair(batteryOptimizationModel, position))
                }
            }
        }
    }

    fun refresh() {
        batteryOptimizationArrayList.clear()
        loadBatteryOptimization()
    }

    fun clearBatteryOptimizationAppData() {
        batteryOptimizationUpdate.value = null
    }

    override fun onShellCreated(shell: Shell?) {
        loadBatteryOptimization()
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        loadBatteryOptimization()
    }
}
