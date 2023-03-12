package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Warnings
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClearDataViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

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
                Shell.cmd("pm clear ${packageInfo.packageName}").submit { shellResult ->
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
                postError(it)
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
                postError(it)
            }
        }
    }

    private fun runShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                ShizukuUtils.execInternal(Command("pm clear ${packageInfo.packageName}"), null).let {
                    result.postValue("\n" + it)
                    Log.d("ClearData", it.toString())
                }
            }.onFailure {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
                postError(it)
            }.onSuccess {
                success.postValue("Done")
            }.getOrElse {
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
                postError(it)
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
}