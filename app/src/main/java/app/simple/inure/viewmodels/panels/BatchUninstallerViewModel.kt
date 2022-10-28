package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.RootViewModel
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.models.BatchUninstallerProgressStateModel
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchUninstallerViewModel(application: Application, val list: ArrayList<BatchPackageInfo>) : RootViewModel(application) {

    private val state = MutableLiveData<BatchUninstallerProgressStateModel>()
    private val done = MutableLiveData(0)
    private val batchUninstallerProgressStateModel = BatchUninstallerProgressStateModel()

    fun getState(): LiveData<BatchUninstallerProgressStateModel> {
        return state
    }

    fun getDone(): LiveData<Int> {
        return done
    }

    init {
        batchUninstallerProgressStateModel.queued = list.size
        state.postValue(batchUninstallerProgressStateModel)
        initShell()
    }

    override fun onShellCreated(shell: Shell?) {
        viewModelScope.launch(Dispatchers.IO) {
            for (app in list) {
                kotlin.runCatching {
                    batchUninstallerProgressStateModel.incrementCount()
                    state.postValue(batchUninstallerProgressStateModel)

                    Shell.cmd(app.packageInfo.getUninstallCommand()).submit {
                        if (it.isSuccess) {
                            if (packageManager.isPackageInstalled(app.packageInfo.packageName).invert()) {
                                batchUninstallerProgressStateModel.incrementDone()
                            }
                        } else {
                            batchUninstallerProgressStateModel.incrementFailed()
                        }
                    }
                }.getOrElse {
                    batchUninstallerProgressStateModel.incrementFailed()
                }

                batchUninstallerProgressStateModel.decrementQueued()
                state.postValue(batchUninstallerProgressStateModel)
            }
        }
    }

    private fun PackageInfo.getUninstallCommand(): String {
        return if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            "pm uninstall -k --user 0 $packageName"
        } else {
            "pm uninstall $packageName"
        }
    }
}