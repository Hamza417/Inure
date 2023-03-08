package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.isAppHidden
import app.simple.inure.constants.Warnings
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HideViewModel(application: Application, private val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

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
                Shell.cmd(formStateCommand()).submit { shellResult ->
                    kotlin.runCatching {
                        for (i in shellResult.out) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception") || i.contains("not exist")) {
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
            }
        }
    }

    private fun runShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                ShizukuUtils.setAppHidden(packageManager.isAppHidden(packageInfo.packageName), setOf(packageInfo.packageName))
            }.onFailure {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }.onSuccess {
                success.postValue("Done")
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }
        }
    }

    private fun formStateCommand(): String {
        val hide = if (packageManager.isAppHidden(packageInfo.packageName)) {
            "unhide"
        } else {
            "hide"
        }

        return "pm $hide ${packageInfo.packageName}"
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        postWarning(Warnings.getInureWarning01())
    }

    override fun onShizukuCreated() {
        runShizuku()
    }
}