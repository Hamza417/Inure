package app.simple.inure.viewmodels.dialogs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.exceptions.InureShellException
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShellExecutorViewModel(application: Application, private val command: String) : AndroidViewModel(application) {

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            runCommand()
        }
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
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
                Shell.enableVerboseLogging = BuildConfig.DEBUG
                Shell.setDefaultBuilder(Shell.Builder.create()
                                                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                .setTimeout(10))

                Shell.cmd(command).submit {
                    kotlin.runCatching {
                        for (i in it.out) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception")) {
                                throw InureShellException("Execution Failed...")
                            }
                        }
                    }.onSuccess {
                        success.postValue("Done")
                    }.getOrElse {
                        result.postValue("\n" + it.message!!)
                    }
                }
            }.onFailure {
                result.postValue("\n" + it.message!!)
            }.getOrElse {
                result.postValue("\n" + it.message!!)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Shell.getShell().close()
    }
}
