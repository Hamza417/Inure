package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Warnings
import app.simple.inure.dialogs.batch.BatchUninstaller.Companion.BatchUninstallerResult
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.BatchPackageInfo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchUninstallerViewModel(application: Application, val list: ArrayList<BatchPackageInfo>) : RootShizukuViewModel(application) {

    private val data: MutableLiveData<ArrayList<BatchUninstallerResult>> by lazy {
        MutableLiveData<ArrayList<BatchUninstallerResult>>().also {
            initializeCoreFramework()
        }
    }

    fun getData(): LiveData<ArrayList<BatchUninstallerResult>> {
        return data
    }

    override fun onShellCreated(shell: Shell?) {
        viewModelScope.launch(Dispatchers.IO) {
            val uninstalledApps = arrayListOf<BatchUninstallerResult>()

            for (app in list) {
                runCatching {
                    Shell.cmd(app.packageInfo.getUninstallCommand()).exec().let {
                        if (it.isSuccess) {
                            uninstalledApps.add(BatchUninstallerResult(app.packageInfo, true))
                        } else {
                            uninstalledApps.add(BatchUninstallerResult(app.packageInfo, false))
                        }
                    }
                }.onFailure {
                    uninstalledApps.add(BatchUninstallerResult(app.packageInfo, false))
                }
            }

            data.postValue(uninstalledApps)
        }
    }

    private fun PackageInfo.getUninstallCommand(): String {
        return if (safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            "pm uninstall -k --user current $packageName"
        } else {
            "pm uninstall $packageName"
        }
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getNoRootConnectionWarning())
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            val uninstalledApps = arrayListOf<BatchUninstallerResult>()

            for (app in list) {
                runCatching {
                    shizukuServiceHelper.service!!.simpleExecute(app.packageInfo.getUninstallCommand()).let {
                        if (it.isSuccess) {
                            uninstalledApps.add(BatchUninstallerResult(app.packageInfo, true))
                        } else {
                            uninstalledApps.add(BatchUninstallerResult(app.packageInfo, false))
                        }
                    }
                }.onFailure {
                    uninstalledApps.add(BatchUninstallerResult(app.packageInfo, false))
                }
            }

            data.postValue(uninstalledApps)
        }
    }
}
