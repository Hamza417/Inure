package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Warnings
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.PermissionInfo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionStatusViewModel(application: Application, val packageInfo: PackageInfo, val permissionInfo: PermissionInfo) : RootShizukuViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getResults(): LiveData<String> {
        return result
    }

    fun getSuccessStatus(): LiveData<String> {
        return success
    }

    private fun runCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.cmd(createCommand()).submit { shellResult ->
                    kotlin.runCatching {
                        for (i in shellResult.out) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception")) {
                                throw InureShellException("Execution Failed...")
                            }
                        }
                    }.onSuccess {
                        if (shellResult.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }.getOrElse {
                        result.postValue("\n" + it.message!!)
                        if (shellResult.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }
                }
            }.onFailure {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }
        }
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
        success.postValue("Failed")
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                shizukuServiceHelper.service!!.simpleExecute(createCommand()).let {
                    result.postValue(it.toString())
                }
            }.onSuccess {
                success.postValue("Done")
            }.onFailure {
                result.postValue("\n" + it.message)
                success.postValue("Failed")
            }.getOrElse {
                result.postValue("\n" + it.message)
                success.postValue("Failed")
            }
        }
    }

    fun setPermissionState(mode: PermissionInfo) {
        this.permissionInfo.isGranted = mode.isGranted
        initializeCoreFramework()
    }

    private fun createCommand(): String {
        val mode = if (this.permissionInfo.isGranted == 1) "revoke" else "grant"
        val user = getCurrentUser()
        return "pm $mode --user $user ${packageInfo.packageName} ${permissionInfo.name}"
    }
}
