package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Warnings
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionStatusViewModel(application: Application, val packageInfo: PackageInfo, val permissionInfo: PermissionInfo) : RootShizukuViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            initializeCoreFramework()
        }
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
                val mode = if (this@PermissionStatusViewModel.permissionInfo.isGranted == 1) "revoke" else "grant"

                Shell.cmd("pm $mode ${packageInfo.packageName} ${permissionInfo.name}").submit { shellResult ->
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

    private fun runShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            val mode = if (this@PermissionStatusViewModel.permissionInfo.isGranted == 1) "revoke" else "grant"

            kotlin.runCatching {
                ShizukuUtils.execInternal(Command("pm $mode ${packageInfo.packageName} ${permissionInfo.name}"), null).let {
                    result.postValue(it.toString())
                }
            }.onSuccess {
                success.postValue("Done")
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
        warning.postValue(Warnings.getInureWarning01())
        success.postValue("Failed")
    }

    override fun onShizukuCreated() {
        runShizuku()
    }

    fun setPermissionState(mode: PermissionInfo) {
        this.permissionInfo.isGranted = mode.isGranted
        if (ConfigurationPreferences.isUsingRoot()) {
            initShell()
        } else {
            initShizuku()
        }
    }
}