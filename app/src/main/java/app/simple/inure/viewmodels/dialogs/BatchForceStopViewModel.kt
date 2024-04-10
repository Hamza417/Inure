package app.simple.inure.viewmodels.dialogs

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.shizuku.Shell.Command
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchForceStopViewModel(application: Application, private val apps: ArrayList<BatchPackageInfo>) : RootShizukuViewModel(application) {

    init {
        initializeCoreFramework()
    }

    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getResults(): MutableLiveData<String> {
        return result
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        super.onShizukuCreated(shizukuServiceHelper)
        runShizuku()
    }

    override fun onShellCreated(shell: Shell?) {
        super.onShellCreated(shell)
        runRootCommand()
    }

    private fun runRootCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                buildString {
                    apps.forEach { batchPackageInfo ->
                        Shell.cmd("am force-stop ${batchPackageInfo.packageInfo.packageName}").exec().let { result ->
                            if (result.isSuccess) {
                                if (isEmpty()) {
                                    append("${getString(R.string.closed)} - ${batchPackageInfo.packageInfo.packageName}")
                                } else {
                                    append("\n")
                                    append("${getString(R.string.closed)} - ${batchPackageInfo.packageInfo.packageName}")
                                }
                            } else {
                                if (isEmpty()) {
                                    append("${getString(R.string.failed)} - ${batchPackageInfo.packageInfo.packageName}")
                                } else {
                                    append("\n")
                                    append("${getString(R.string.failed)} - ${batchPackageInfo.packageInfo.packageName}")
                                }
                            }
                        }
                    }

                    this@BatchForceStopViewModel.result.postValue(this.toString())
                }
            }.onFailure {
                result.postValue(it.message ?: "Failed to force stop apps")
            }
        }
    }

    private fun runShizuku() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                buildString {
                    apps.forEach { batchPackageInfo ->
                        ShizukuUtils.execInternal(Command(
                                "am force-stop ${batchPackageInfo.packageInfo.packageName}"), null).let { result ->
                            if (result.isSuccess) {
                                if (isEmpty()) {
                                    append("${getString(R.string.closed)} - ${batchPackageInfo.packageInfo.packageName}")
                                } else {
                                    append("\n")
                                    append("${getString(R.string.closed)} - ${batchPackageInfo.packageInfo.packageName}")
                                }
                            } else {
                                if (isEmpty()) {
                                    append("${getString(R.string.failed)} - ${batchPackageInfo.packageInfo.packageName}")
                                } else {
                                    append("\n")
                                    append("${getString(R.string.failed)} - ${batchPackageInfo.packageInfo.packageName}")
                                }
                            }
                        }
                    }

                    this@BatchForceStopViewModel.result.postValue(this.toString())
                }
            }.onFailure {
                result.postValue(it.message ?: "Failed to force stop apps")
            }
        }
    }
}
