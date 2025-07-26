package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Warnings
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UninstallerViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            initializeCoreFramework()
        }
    }

    fun getSuccessStatus(): LiveData<String> {
        return success
    }

    private fun runCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.cmd(formUninstallCommand()).submit { shellResult ->
                    if (shellResult.isSuccess) {
                        success.postValue("Done")
                    } else {
                        success.postValue("Failed")
                        val sb = StringBuilder()
                        for (s in shellResult.out) {
                            sb.append(s)
                            sb.append("\t")
                        }
                        postWarning(sb.toString())
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

    private fun formUninstallCommand(): String {
        return if (packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            "pm uninstall --user current ${packageInfo.packageName}"
        } else {
            "pm uninstall ${packageInfo.packageName}"
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
                shizukuServiceHelper.service!!.simpleExecute(formUninstallCommand()).let {
                    Log.d("Shizuku", it.toString())
                }
            }.onFailure {
                success.postValue("Failed")
                postError(it)
            }.onSuccess {
                success.postValue("Done")
            }.getOrElse {
                success.postValue("Failed")
                postError(it)
            }
        }
    }
}
