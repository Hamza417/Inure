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
import app.simple.inure.helpers.ShizukuServiceHelper
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForceCloseViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {
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
                Shell.cmd("am force-stop ${packageInfo.packageName}").submit { shellResult ->
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

    private fun runShizuku() {
        kotlin.runCatching {
            Log.d("ForceCloseViewModel", "Running Shizuku command...")
            ShizukuServiceHelper().service?.execute(mutableListOf("am", "force-stop", packageInfo.packageName), null, null).let {
                Log.d("ForceCloseViewModel", it?.toString()!!)
                if (it.isSuccess) {
                    result.postValue(it.output)
                    Log.d("ForceCloseViewModel", it.output.toString())
                } else {
                    result.postValue(it.error)
                    Log.d("ForceCloseViewModel", it.error.toString())
                }
            }
        }.onFailure {
            it.printStackTrace()
            result.postValue("\n" + it.message)
            success.postValue("Failed")
        }.onSuccess {
            success.postValue("Done")
        }.getOrElse {
            it.printStackTrace()
            result.postValue("\n" + it.message)
            success.postValue("Failed")
        }
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
        success.postValue("Failed")
    }

    override fun onShizukuCreated() {
        runShizuku()
    }
}