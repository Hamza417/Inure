package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Warnings
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootViewModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StateViewModel(application: Application, private val packageInfo: PackageInfo) : RootViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            if (ConfigurationPreferences.isUsingRoot()) {
                initShell()
            } else if (ConfigurationPreferences.isUsingShizuku()) {
                initShizuku()
            }
        }
    }

    private fun initShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                ShizukuUtils.setAppDisabled(packageInfo.applicationInfo.enabled, setOf(packageInfo.packageName))
            }.onFailure {
                Log.e("StateViewModel", it.message.toString())
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }.onSuccess {
                result.postValue("\n" + it)
                success.postValue("Done")
                Log.d("StateViewModel", "Success: Disabled: ${packageManager.getApplicationInfo(packageInfo.packageName, 0).enabled} Package: ${packageInfo.packageName}")
            }.getOrElse {
                Log.e("StateViewModel", it.message.toString())
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }
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
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }
        }
    }

    private fun formStateCommand(): String {
        return if (packageInfo.applicationInfo.enabled) {
            "pm disable ${packageInfo.packageName}"
        } else {
            "pm enable ${packageInfo.packageName}"
        }
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getInureWarning01())
        success.postValue("Failed")
    }
}