package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReinstallerViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            initializeCoreFramework()
        }
    }

    fun getSuccessStatus(): LiveData<String> {
        return success
    }

    private fun runCommand() {
        Shell.cmd(getReinstallCommand()).submit { shellResult ->
            if (shellResult.isSuccess) {
                success.postValue("Done")
            } else {
                if (shellResult.err.contains(ERR_3001) || shellResult.out.contains(ERR_3001)) {
                    Shell.cmd(getInstallExistingCommand()).exec().let { result ->
                        if (result.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }
                } else {
                    success.postValue("Failed")
                }
            }
        }
    }

    private fun getReinstallCommand(): String {
        return "pm install -r ${packageInfo.applicationInfo.sourceDir}"
    }

    private fun getInstallExistingCommand(): String {
        return "pm install-existing ${packageInfo.packageName}"
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                shizukuServiceHelper.service!!.simpleExecute(getReinstallCommand()).let {
                    if (it.isSuccess) {
                        success.postValue("Done")
                    } else {
                        if (it.error?.contains(ERR_3001) == true || it.output?.contains(ERR_3001) == true) {
                            shizukuServiceHelper.service!!.simpleExecute(getInstallExistingCommand()).let { result ->
                                if (result.isSuccess) {
                                    success.postValue("Done")
                                } else {
                                    success.postValue("Failed")
                                    postWarning(result.error + "\n" + result.output)
                                }
                            }
                        } else {
                            success.postValue("Failed")
                            postWarning(it.error + "\n" + it.output)
                        }
                    }
                }
            }.onFailure {
                success.postValue("Failed")
                postError(it)
            }.getOrElse {
                success.postValue("Failed")
                postError(it)
            }
        }
    }

    companion object {
        private const val ERR_3001 = "INSTALL_FAILED_REJECTED_BY_BUILDTYPE"
    }
}
