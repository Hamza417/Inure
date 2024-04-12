package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatchPackageInfo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchBatteryOptimizationViewModel(application: Application, val apps: ArrayList<BatchPackageInfo>) : RootShizukuViewModel(application) {

    private var isOptimized: Boolean = false

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getResult(): LiveData<String> {
        return result
    }

    fun init(isOptimized: Boolean) {
        initializeCoreFramework()
        this.isOptimized = isOptimized
    }

    private fun createCommand(): String {
        val command = StringBuilder()

        if (isOptimized) {
            if (apps.size == 1) {
                command.append("dumpsys deviceidle whitelist -${apps[0].packageInfo.packageName}")
            } else {
                for (app in apps) {
                    command.append("dumpsys deviceidle whitelist -${app.packageInfo.packageName} && ")
                }
            }
        } else {
            if (apps.size == 1) {
                command.append("dumpsys deviceidle whitelist +${apps[0].packageInfo.packageName}")
            } else {
                for (app in apps) {
                    command.append("dumpsys deviceidle whitelist +${app.packageInfo.packageName} && ")
                }
            }
        }

        return command.removeSuffix(" && ").toString() // remove the last " && " :)
    }

    private fun runSuCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("BatchBatteryOptimizationViewModel", "runSuCommand: ${createCommand()}")
            Shell.cmd(createCommand()).exec().let {
                Log.d("BatchBatteryOptimizationViewModel", "runSuCommand: ${it.out}")
                result.postValue(it.out.toString())
            }
        }
    }

    private fun runShizukuCommand(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                shizukuServiceHelper.service!!.simpleExecute(createCommand()).let {
                    Log.d("BatchBatteryOptimizationViewModel", "runShizukuCommand: ${it.output}")
                    result.postValue(it.output.toString())
                }
            }.onFailure {
                Log.d("BatchBatteryOptimizationViewModel", "runShizukuCommand: ${it.message}")
                postError(it)
            }
        }
    }

    override fun onShellCreated(shell: Shell?) {
        runSuCommand()
    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        runShizukuCommand(shizukuServiceHelper)
    }
}
