package app.simple.inure.viewmodels.panels

import android.app.Application
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
            Shell.cmd(command).submit { result ->
                if (result.isSuccess) {
                    val packageNames = result.out.map { it.split("/")[0] }.stream().distinct().collect(Collectors.toList())

                    val bootManagerModelArrayList = ArrayList<BootManagerModel>()
                    packageNames.forEach { packageName ->
                        val bootManagerModel = BootManagerModel()
                        bootManagerModel.packageName = packageName
                        bootManagerModel.name = PackageUtils.getApplicationName(applicationContext(), bootManagerModel.packageName)
                        bootManagerModel.isEnabled = packageManager.isPackageInstalledAndEnabled(bootManagerModel.packageName)

                        result.out.forEach {
                            if (it.startsWith(bootManagerModel.packageName)) {
                                val componentName = bootManagerModel.packageName + it.substringAfter("/")
                                if (ReceiversUtils.isEnabled(applicationContext(), bootManagerModel.packageName, componentName)) {
                                    bootManagerModel.addEnabledComponent(componentName)
                                } else {
                                    bootManagerModel.addDisabledComponent(componentName)
                                }
                            }
                        }

                        Log.d("BootManagerViewModel", "loadBootComponents: $bootManagerModel")
                        bootManagerModelArrayList.add(bootManagerModel)
                    }

                    bootManagerModelArrayList.sortBy {
                        it.name
                    }

                    bootComponentData.postValue(bootManagerModelArrayList)
                } else {
                    Log.d("BootManagerViewModel", "loadBootComponents: ${result.err}")
                }
            }
        }
    }

    fun enableAllComponents(bootManagerModel: BootManagerModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            var err = 0

            bootManagerModel.disabledComponents.forEach { disabledComponent -> // Unnecessary but just to be sure :D
                Shell.cmd("pm enable $disabledComponent").submit {
                    if (it.isSuccess) {
                        bootManagerModel.enabledComponents.add(disabledComponent)
                    } else {
                        err++
                    }
                }
            }.also {
                if (err == 0) {
                    bootManagerModel.disabledComponents.clear()
                    bootManagerModelData.postValue(Pair(bootManagerModel, position))
                }
            }
        }
    }

    fun disableAllComponents(bootManagerModel: BootManagerModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            var err = 0

            bootManagerModel.enabledComponents.forEach { enabledComponent ->
                Shell.cmd("pm disable $enabledComponent").submit {
                    if (it.isSuccess) {
                        bootManagerModel.disabledComponents.add(enabledComponent)
                    } else {
                        err++
                    }
                }
            }.also {
                if (err == 0) {
                    bootManagerModel.enabledComponents.clear()
                    bootManagerModelData.postValue(Pair(bootManagerModel, position))
                }
            }
        }
    }

    fun clearBootManagerModelData() {
        bootManagerModelData.postValue(null)
    }
}