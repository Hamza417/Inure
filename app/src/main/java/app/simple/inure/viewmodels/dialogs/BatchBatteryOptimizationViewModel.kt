package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchBatteryOptimizationViewModel(application: Application, val apps: ArrayList<BatchPackageInfo>) : RootShizukuViewModel(application) {

    private var isOptimized: Boolean = false

    private val done: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getDone(): LiveData<Int> {
        return done
    }

    fun init(isOptimized: Boolean) {
        initializeCoreFramework()
        this.isOptimized = isOptimized
    }

    private fun createCommand(): String {
        val command = StringBuilder()

        if (isOptimized) {
            for (app in apps) {
                command.append("dumpsys deviceidle whitelist +${app.packageInfo.packageName} && ")
            }
        } else {
            for (app in apps) {
                command.append("dumpsys deviceidle whitelist -${app.packageInfo.packageName} && ")
            }
        }

        command.removeSuffix(" && ") // remove the last " && " :)

        return command.toString()
    }

    private fun runSuCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("BatchBatteryOptimizationViewModel", "runSuCommand: ${createCommand()}")
            Shell.cmd(createCommand()).exec().let {
                Log.d("BatchBatteryOptimizationViewModel", "runSuCommand: ${it.out}")
                if (it.isSuccess) {
                    done.postValue((0..100).random())
                } else {
                    done.postValue(-1)
                }
            }
        }
    }

    private fun runShizukuCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                ShizukuUtils.execInternal(Command(createCommand(), null), null).let {
                    Log.d("BatchBatteryOptimizationViewModel", "runShizukuCommand: ${it.out}")
                }
            }.onSuccess {
                done.postValue((0..100).random())
            }.onFailure {
                done.postValue(-1)
            }
        }
    }

    override fun onShellCreated(shell: Shell?) {
        runSuCommand()
    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated() {
        runShizukuCommand()
    }
}