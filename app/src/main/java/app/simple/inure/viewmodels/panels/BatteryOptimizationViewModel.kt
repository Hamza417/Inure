package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getInstalledPackages
import app.simple.inure.extensions.viewmodels.RootViewModel
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.util.NullSafety.isNotNull
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryOptimizationViewModel(application: Application) : RootViewModel(application) {

    init {
        initShell()
    }

    private val batteryOptimizationArrayList = ArrayList<BatteryOptimizationModel>()

    private val batteryOptimizationData: MutableLiveData<ArrayList<BatteryOptimizationModel>> by lazy {
        MutableLiveData<ArrayList<BatteryOptimizationModel>>()
    }

    fun getBatteryOptimizationData(): LiveData<ArrayList<BatteryOptimizationModel>> {
        return batteryOptimizationData
    }

    private fun loadBatteryOptimization() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = packageManager.getInstalledPackages()

            kotlin.runCatching {
                Shell.cmd("dumpsys deviceidle whitelist").exec().let { result ->
                    if (result.isSuccess) {
                        apps.forEach { packageInfo ->
                            val outData = result.out.find { out ->
                                packageInfo.packageName == out.subSequence(out.indexOf(",").plus(1), out.lastIndexOf(",")).trim()
                            }

                            if (outData.isNotNull()) {
                                val batteryOptimizationModel = BatteryOptimizationModel()

                                val type = outData!!.substring(outData.indexOf(",")).trim()
                                // val packageName = outData.subSequence(outData.indexOf(",").plus(1), outData.lastIndexOf(",")).trim()
                                // val uid = outData.subSequence(outData.lastIndexOf(",").plus(1), outData.length).trim()

                                batteryOptimizationModel.packageInfo = packageInfo
                                batteryOptimizationModel.type = type
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

                        batteryOptimizationData.postValue(batteryOptimizationArrayList)
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun setBatteryOptimization(packageName: String, state: Boolean) {
        Shell.cmd("dumpsys deviceidle whitelist ${if (state) "+" else "-"}$packageName").exec().let {
            if (it.isSuccess) {
                loadBatteryOptimization()
            }
        }
    }

    fun onBatteryOptimizationChange(packageName: String, state: Boolean) {
        setBatteryOptimization(packageName, state)
    }

    override fun onShellCreated(shell: Shell?) {
        loadBatteryOptimization()
    }
}