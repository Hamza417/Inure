package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.apk.ops.AppOps
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.AppOpsModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val appOpsData: MutableLiveData<ArrayList<AppOpsModel>> by lazy {
        MutableLiveData<ArrayList<AppOpsModel>>().also {
            loadAppOpsData("")
        }
    }

    private val appOpsState: MutableLiveData<Pair<AppOpsModel, Int>> by lazy {
        MutableLiveData<Pair<AppOpsModel, Int>>()
    }

    fun getAppOpsData(): LiveData<ArrayList<AppOpsModel>> {
        return appOpsData
    }

    fun getAppOpsState(): LiveData<Pair<AppOpsModel, Int>> {
        return appOpsState
    }

    fun loadAppOpsData(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ops = AppOps.getOps(context, packageInfo.packageName)
            val filtered = arrayListOf<AppOpsModel>()

            for (op in ops) {
                if (op.title.lowercase().contains(keyword) || op.description.lowercase().contains(keyword)) {
                    filtered.add(op)
                }
            }

            appOpsData.postValue(filtered)
        }
    }

    fun updateAppOpsState(appsOpsModel: AppOpsModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                if (ConfigurationPreferences.isUsingRoot()) {
                    kotlin.runCatching {
                        Shell.enableVerboseLogging = BuildConfig.DEBUG
                        Shell.setDefaultBuilder(Shell.Builder.create()
                                                    .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                    .setTimeout(10))
                    }

                    Shell.cmd(getStateChangeCommand(appsOpsModel)).exec()
                } else {
                    Runtime.getRuntime().exec(getStateChangeCommand(appsOpsModel))
                }
            }.onSuccess {
                appsOpsModel.isEnabled = appsOpsModel.isEnabled.invert()
                appOpsState.postValue(Pair(appsOpsModel, position))
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getStateChangeCommand(appsOpsModel: AppOpsModel): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("appops set ")
        stringBuilder.append(packageInfo.packageName)
        stringBuilder.append(" ")
        stringBuilder.append(appsOpsModel.title + if (appsOpsModel.isEnabled) " deny" else " allow")
        return stringBuilder.toString()
    }
}