package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Warnings
import app.simple.inure.dialogs.batch.BatchUninstaller.Companion.BatchUninstallerResult
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatchPackageInfo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatchUninstallerViewModel(application: Application, val list: ArrayList<BatchPackageInfo>) : RootShizukuViewModel(application) {

    private val _uninstallResults = MutableStateFlow<ArrayList<BatchUninstallerResult>>(arrayListOf())
    val uninstallResults: StateFlow<ArrayList<BatchUninstallerResult>> = _uninstallResults.asStateFlow()

    init {
        // Initialize with pending state for all apps
        val initialResults = ArrayList(list.map { BatchUninstallerResult(it.packageInfo, null) })
        _uninstallResults.value = initialResults
        initializeCoreFramework()
    }

    override fun onShellCreated(shell: Shell?) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = ArrayList(_uninstallResults.value)

            for ((index, app) in list.withIndex()) {
                runCatching {
                    Shell.cmd(app.packageInfo.getUninstallCommand()).exec().let {
                        results[index] = BatchUninstallerResult(app.packageInfo, it.isSuccess)
                        _uninstallResults.value = ArrayList(results)
                    }
                }.onFailure {
                    results[index] = BatchUninstallerResult(app.packageInfo, false)
                    _uninstallResults.value = ArrayList(results)
                }
            }
        }
    }

    private fun PackageInfo.getUninstallCommand(): String {
        return if (safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            "pm uninstall -k --user current $packageName"
        } else {
            "pm uninstall --user current $packageName"
        }
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = ArrayList(_uninstallResults.value)

            for ((index, app) in list.withIndex()) {
                runCatching {
                    shizukuServiceHelper.service!!.simpleExecute(app.packageInfo.getUninstallCommand()).let {
                        results[index] = BatchUninstallerResult(app.packageInfo, it.isSuccess)
                        _uninstallResults.value = ArrayList(results)
                    }
                }.onFailure {
                    results[index] = BatchUninstallerResult(app.packageInfo, false)
                    _uninstallResults.value = ArrayList(results)
                }
            }
        }
    }
}
