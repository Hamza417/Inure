package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.constants.Misc
import app.simple.inure.exceptions.InureShellException
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityLauncherViewModel(application: Application, val packageInfo: PackageInfo, val packageId: String) : AndroidViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            runCommand()
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
            delay(Misc.delay)

            kotlin.runCatching {
                Shell.enableVerboseLogging = BuildConfig.DEBUG
                Shell.setDefaultBuilder(Shell.Builder.create()
                                                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                .setTimeout(10))

                Shell.su(formLaunchCommand(null)).submit { shellResult ->
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

    fun runActionCommand(action: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(Misc.delay)

            kotlin.runCatching {
                Shell.enableVerboseLogging = BuildConfig.DEBUG
                Shell.setDefaultBuilder(Shell.Builder.create()
                                                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                .setTimeout(10))

                Shell.su(formLaunchCommand(action)).submit { shellResult ->
                    kotlin.runCatching {
                        for (i in shellResult.out) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception") || i.contains("not exist")) {
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

    private fun formLaunchCommand(action: String?): String {
        return "am start -n ${packageInfo.packageName}/$packageId -a \"${action ?: "android.intent.action.MAIN"}\""
    }

    override fun onCleared() {
        super.onCleared()
        Shell.getShell().close()
    }
}