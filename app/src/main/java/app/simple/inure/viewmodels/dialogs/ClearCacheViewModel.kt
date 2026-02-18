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

class ClearCacheViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {
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
                Shell.cmd(getCommand()).submit { shellResult ->
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
                        it.printStackTrace()
                        result.postValue("\n" + it.message)
                        if (shellResult.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
                result.postValue("\n" + it.message)
                success.postValue("Failed")
            }.getOrElse {
                it.printStackTrace()
                result.postValue("\n" + it.message)
                success.postValue("Failed")
            }
        }
    }

    private fun runShizuku(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                if (shizukuServiceHelper.isRootMode().not()) {
                    postWarning("Shizuku is not running in root mode and root is required to clear app cache.")
                    return@launch
                }

                shizukuServiceHelper.service?.simpleExecute(getCommand()).let { shellResult ->
                    kotlin.runCatching {
                        for (i in shellResult?.output!!.lines()) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception") || i.contains("not exist")) {
                                throw InureShellException("Execution Failed...")
                            }
                        }
                    }.onSuccess {
                        if (shellResult?.isSuccess == true) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }.getOrElse {
                        it.printStackTrace()
                        result.postValue("\n" + it.message)
                        if (shellResult?.isSuccess == true) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }
                }
            }.onSuccess {
                Log.d("ClearCacheViewModel", "Cache cleared: ${packageInfo.packageName}")
                success.postValue("Done")
            }.onFailure {
                it.printStackTrace()
                result.postValue("\n" + it.message)
                success.postValue("Failed")
            }.getOrElse {
                it.printStackTrace()
                result.postValue("\n" + it.message)
                success.postValue("Failed")
            }
        }
    }

    private fun getCommand(): String {
        return "pm clear --cache-only --user ${getCurrentUser()} ${packageInfo.packageName}"
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
        success.postValue("Failed")
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        runShizuku(shizukuServiceHelper)
    }
}
