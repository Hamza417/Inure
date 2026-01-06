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
import app.simple.inure.preferences.ConfigurationPreferences
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityLauncherViewModel(application: Application, val packageInfo: PackageInfo, val packageId: String) : RootShizukuViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            initializeCoreFramework()
        }
    }

    private val action: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getResults(): LiveData<String> {
        return result
    }

    fun getSuccessStatus(): LiveData<String> {
        return success
    }

    fun getActionStatus(): LiveData<String> {
        return action
    }

    private fun runCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.cmd(formLaunchCommand(null)).submit { shellResult ->
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
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }
        }
    }

    private fun runActionCommand(action: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.cmd(formLaunchCommand(action)).submit { shellResult ->
                    kotlin.runCatching {
                        for (s in shellResult.out) {
                            result.postValue("\n" + s)
                            if (s.contains("Exception") || s.contains("not exist")) {
                                throw InureShellException("Execution Failed...")
                            }
                        }
                    }.onSuccess {
                        if (shellResult.isSuccess) {
                            this@ActivityLauncherViewModel.action.postValue("Done")
                        } else {
                            this@ActivityLauncherViewModel.action.postValue("Failed")
                        }
                    }.getOrElse {
                        result.postValue("\n" + it.message!!)
                        if (shellResult.isSuccess) {
                            this@ActivityLauncherViewModel.action.postValue("Done")
                        } else {
                            this@ActivityLauncherViewModel.action.postValue("Failed")
                        }
                    }
                }
            }.onFailure {
                result.postValue("\n" + it.message!!)
                this@ActivityLauncherViewModel.action.postValue("Failed")
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                this@ActivityLauncherViewModel.action.postValue("Failed")
            }
        }
    }

    private fun runShizuku(action: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                getShizukuService().simpleExecute(formLaunchCommand(action))
            }.onSuccess {
                success.postValue("Done")
            }.onFailure {
                success.postValue("Failed")
            }.getOrElse {
                success.postValue("Failed")
            }
        }
    }

    private fun formLaunchCommand(action: String?): String {
        val act = action ?: "android.intent.action.MAIN"
        return buildString {
            append("am start --user ")
            append(getCurrentUser())
            append(" -n ")
            append(packageInfo.packageName)
            append('/')
            append(packageId)
            append(" -a \"")
            append(act)
            append('"')
        }
    }

    fun runWithAction(action: String?) {
        if (ConfigurationPreferences.isUsingRoot()) {
            runActionCommand(action)
        } else {
            runShizuku(action)
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
        runShizuku()
    }
}
