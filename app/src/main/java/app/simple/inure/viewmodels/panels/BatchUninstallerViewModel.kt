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
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchUninstallerViewModel(application: Application, val list: ArrayList<BatchPackageInfo>) : RootViewModel(application) {

    private val failed = MutableLiveData(0)
    private val success = MutableLiveData(0)
    private val done = MutableLiveData(0)

    fun getFailed(): LiveData<Int> {
        return failed
    }

    fun getSuccess(): LiveData<Int> {
        return success
    }

    fun getDone(): LiveData<Int> {
        return done
    }

    override fun onShellCreated(shell: Shell?) {
        viewModelScope.launch(Dispatchers.IO) {
            for (app in list) {
                kotlin.runCatching {
                    Shell.cmd(app.packageInfo.getUninstallCommand()).submit {
                        if (it.isSuccess) {
                            if (app.packageInfo.isPackageInstalled(packageManager).invert()) {
                                success.postValue(success.value ?: 0.plus(1))
                            }
                        } else {
                            failed.postValue(failed.value ?: 0.plus(1))
                        }
                    }
                }
            }

            done.postValue(/* value = */ (1..10).random())
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