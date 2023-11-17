package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.ops.AppOps
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.AppOpsModel
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val appOpsData: MutableLiveData<ArrayList<AppOpsModel>> by lazy {
        MutableLiveData<ArrayList<AppOpsModel>>().also {
            initShell()
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
                Shell.cmd(getStateChangeCommand(appsOpsModel)).exec().let {
                    if (it.isSuccess) {
                        appsOpsModel.isEnabled = appsOpsModel.isEnabled.invert()
                        appOpsState.postValue(Pair(appsOpsModel, position))
                    } else {
                        appOpsState.postValue(Pair(appsOpsModel, position))
                        postWarning("Failed to change state of ${appsOpsModel.title} : ${!appsOpsModel.isEnabled} for ${packageInfo.packageName})")
                    }
                }
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

    override fun onShellCreated(shell: Shell?) {
        loadAppOpsData("")
    }

    override fun onShellDenied() {
        /* no-op */
    }

    override fun onShizukuCreated() {

    }
}