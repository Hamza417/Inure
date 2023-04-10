package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.shizuku.ShizukuUtils
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
        Shell.cmd(formReinstallCommand()).submit { shellResult ->
            if (shellResult.isSuccess) {
                success.postValue("Done")
            } else {
                success.postValue("Failed")
            }
        }
    }

    private fun runShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                ShizukuUtils.execInternal(app.simple.inure.shizuku.Shell.Command(formReinstallCommand()), null)
            }.onSuccess {
                success.postValue("Done")
            }.onFailure {
                success.postValue("Failed")
                postError(it)
            }.getOrElse {
                success.postValue("Failed")
                postError(it)
            }
        }
    }

    private fun formReinstallCommand(): String {
        return "pm install -r ${packageInfo.applicationInfo.sourceDir}"
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated() {
        runShizuku()
    }
}