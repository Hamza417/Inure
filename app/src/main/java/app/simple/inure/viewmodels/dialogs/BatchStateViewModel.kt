package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatchPackageInfo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchStateViewModel(application: Application, val list: ArrayList<BatchPackageInfo>, val state: Boolean) : RootShizukuViewModel(application) {

    private val success: MutableLiveData<Int> = MutableLiveData(0)

    fun getSuccess(): MutableLiveData<Int> {
        return success
    }

    init {
        initializeCoreFramework()
    }

    override fun onShellCreated(shell: Shell?) {
        super.onShellCreated(shell)
        runRootCommand()
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        super.onShizukuCreated(shizukuServiceHelper)
        viewModelScope.launch(Dispatchers.IO) {
            val stateCommand = if (state) "enable" else "disable"

            for (app in list) {
                shizukuServiceHelper.service!!.simpleExecute("pm $stateCommand ${app.packageInfo.packageName}").let {
                    if (it!!.isSuccess) {
                        app.packageInfo.applicationInfo.enabled = state
                    } else {
                        Log.e("BatchStateViewModel", "Failed to $stateCommand ${app.packageInfo.packageName}")
                    }
                }
            }

            success.postValue(list.count { it.packageInfo.applicationInfo.enabled == state })
        }
    }

    private fun runRootCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            val stateCommand = if (state) "enable" else "disable"

            for (app in list) {
                Shell.cmd("pm $stateCommand ${app.packageInfo.packageName}").exec().let {
                    if (it.isSuccess) {
                        app.packageInfo.applicationInfo.enabled = state
                    } else {
                        Log.e("BatchStateViewModel", "Failed to $stateCommand ${app.packageInfo.packageName}")
                    }
                }
            }

            success.postValue(list.count { it.packageInfo.applicationInfo.enabled == state })
        }
    }

    private fun formStateCommand(): String {
        return if (state) {
            list.getEnableCommand()
        } else {
            list.getDisableCommand()
        }
    }

    private fun ArrayList<BatchPackageInfo>.getEnableCommand(): String {
        buildString {
            for (app in this@getEnableCommand) {
                append("pm enable ${app.packageInfo.packageName} && ")
            }

            removeSuffix(" && ")

            return this.toString()
        }
    }

    private fun ArrayList<BatchPackageInfo>.getDisableCommand(): String {
        buildString {
            for (app in this@getDisableCommand) {
                append("pm disable ${app.packageInfo.packageName} && ")
            }

            removeSuffix(" && ")

            return this.toString()
        }
    }
}
