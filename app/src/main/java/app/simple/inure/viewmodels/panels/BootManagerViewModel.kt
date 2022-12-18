package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalledAndEnabled
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.extensions.viewmodels.RootViewModel
import app.simple.inure.models.BootManagerModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class BootManagerViewModel(application: Application) : RootViewModel(application) {

    private val command = "pm query-receivers --components -a android.intent.action.BOOT_COMPLETED"
    private val bootCompletedIntent = "android.intent.action.BOOT_COMPLETED"

    private val resolveInfoFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.MATCH_DIRECT_BOOT_AWARE or PackageManager.MATCH_DIRECT_BOOT_UNAWARE or
                PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS or
                PackageManager.GET_RECEIVERS
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS or
                PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS
    }

    private val bootComponentData: MutableLiveData<ArrayList<BootManagerModel>> by lazy {
        MutableLiveData<ArrayList<BootManagerModel>>()
    }

    private val bootManagerModelData: MutableLiveData<Pair<BootManagerModel, Int>> by lazy {
        MutableLiveData<Pair<BootManagerModel, Int>>()
    }

    fun getBootComponentData(): LiveData<ArrayList<BootManagerModel>> {
        return bootComponentData
    }

    fun getBootManagerModelData(): LiveData<Pair<BootManagerModel, Int>> {
        return bootManagerModelData
    }

    init {
        initShell()
    }

    override fun onShellCreated(shell: Shell?) {
        Log.d("BootManagerViewModel", "onShellCreated: SHELL CREATED")
        loadBootComponents()
    }

    private fun loadBootComponents() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), PackageManager.ResolveInfoFlags.of(resolveInfoFlags.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), resolveInfoFlags)
                }
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), resolveInfoFlags)
            }

            val bootManagerModelArrayList = ArrayList<BootManagerModel>()
            val packageNames = list.stream().map { it.activityInfo.packageName }.collect(Collectors.toList()).distinct()

            packageNames.forEach { packageName ->
                val bootManagerModel = BootManagerModel()
                bootManagerModel.packageName = packageName
                bootManagerModel.name = PackageUtils.getApplicationName(applicationContext(), bootManagerModel.packageName)
                bootManagerModel.isEnabled = packageManager.isPackageInstalledAndEnabled(bootManagerModel.packageName)

                list.forEach { resolveInfo ->
                    if (resolveInfo.activityInfo.packageName.equals(bootManagerModel.packageName)) {
                        val componentName = resolveInfo.activityInfo.name
                        if (ReceiversUtils.isEnabled(applicationContext(), bootManagerModel.packageName, componentName)) {
                            bootManagerModel.addEnabledComponent(componentName)
                        } else {
                            bootManagerModel.addDisabledComponent(componentName)
                        }
                    }
                }

                bootManagerModelArrayList.add(bootManagerModel)
            }

            bootManagerModelArrayList.sortBy {
                it.name
            }

            bootComponentData.postValue(bootManagerModelArrayList)

            // This method is also valid but it's not as efficient as the above method
            //
            //            Shell.cmd(command).exec().let { result ->
            //                if (result.isSuccess) {
            //                    val packageNames = result.out.map { it.split("/")[0] }.stream().distinct().collect(Collectors.toList())
            //
            //                    val bootManagerModelArrayList = ArrayList<BootManagerModel>()
            //                    packageNames.forEach { packageName ->
            //                        val bootManagerModel = BootManagerModel()
            //                        bootManagerModel.packageName = packageName
            //                        bootManagerModel.name = PackageUtils.getApplicationName(applicationContext(), bootManagerModel.packageName)
            //                        bootManagerModel.isEnabled = packageManager.isPackageInstalledAndEnabled(bootManagerModel.packageName)
            //
            //                        result.out.forEach {
            //                            if (it.startsWith(bootManagerModel.packageName)) {
            //                                val componentName = it.substringAfter("/")
            //                                if (ReceiversUtils.isEnabled(applicationContext(), bootManagerModel.packageName, componentName)) {
            //                                    bootManagerModel.addEnabledComponent(componentName)
            //                                } else {
            //                                    bootManagerModel.addDisabledComponent(componentName)
            //                                }
            //                            }
            //                        }
            //
            //                        bootManagerModelArrayList.add(bootManagerModel)
            //                    }
            //
            //                    bootManagerModelArrayList.sortBy {
            //                        it.name
            //                    }
            //
            //                    bootComponentData.postValue(bootManagerModelArrayList)
            //                } else {
            //                    Log.d("BootManagerViewModel", "loadBootComponents: ${result.err}")
            //                }
            //            }
        }
    }

    fun enableAllComponents(bootManagerModel: BootManagerModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            (bootManagerModel.enabledComponents + bootManagerModel.disabledComponents).forEach { component ->
                Shell.cmd("pm enable ${bootManagerModel.packageName}/$component").exec().let { result ->
                    if (result.isSuccess) {
                        Log.d("BootManagerViewModel", "enableAllComponents: $component")
                    } else {
                        Log.e("BootManagerViewModel", "enableAllComponents: ${result.err}")
                    }
                }
            }

            // Verify if all components are enabled ----------------------------------------------------------------------- |

            (bootManagerModel.enabledComponents + bootManagerModel.disabledComponents).forEach { component ->
                if (ReceiversUtils.isEnabled(applicationContext(), bootManagerModel.packageName, component)) {
                    bootManagerModel.addEnabledComponent(component)
                    bootManagerModel.disabledComponents.remove(component)
                } else {
                    bootManagerModel.addDisabledComponent(component)
                    bootManagerModel.enabledComponents.remove(component)
                }
            }

            bootManagerModelData.postValue(Pair(bootManagerModel, position))
        }
    }

    fun disableAllComponents(bootManagerModel: BootManagerModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            (bootManagerModel.enabledComponents + bootManagerModel.disabledComponents).forEach { component ->
                Shell.cmd("pm disable ${bootManagerModel.packageName}/$component").exec().let { result ->
                    if (result.isSuccess) {
                        Log.d("BootManagerViewModel", "disabledComponent: $component")
                    } else {
                        Log.e("BootManagerViewModel", "disabledComponent: ${result.err}")
                    }
                }
            }

            // Verify if all components are disabled ----------------------------------------------------------------------- |

            (bootManagerModel.enabledComponents + bootManagerModel.disabledComponents).forEach { component ->
                if (ReceiversUtils.isEnabled(applicationContext(), bootManagerModel.packageName, component)) {
                    bootManagerModel.addEnabledComponent(component)
                    bootManagerModel.disabledComponents.remove(component)
                } else {
                    bootManagerModel.addDisabledComponent(component)
                    bootManagerModel.enabledComponents.remove(component)
                }
            }

            bootManagerModelData.postValue(Pair(bootManagerModel, position))
        }
    }

    fun clearBootManagerModelData() {
        bootManagerModelData.value = null
    }
}