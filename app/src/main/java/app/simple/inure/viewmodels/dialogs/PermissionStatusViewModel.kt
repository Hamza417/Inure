package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootViewModel
import app.simple.inure.models.PermissionInfo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionStatusViewModel(application: Application, val packageInfo: PackageInfo, val permissionInfo: PermissionInfo, val mode: String?) : RootViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            initShell()
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
                val mode = if (mode == getApplication<Application>().getString(R.string.revoke)) "revoke" else "grant"

                Shell.cmd("pm $mode ${packageInfo.packageName} ${permissionInfo.name}").submit { shellResult ->
                    kotlin.runCatching {
                        for (i in shellResult.out) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception")) {
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

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }
}