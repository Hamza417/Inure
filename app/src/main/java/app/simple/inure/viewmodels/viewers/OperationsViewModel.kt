package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.ops.AppOps
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.AppOp
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val appOpsData: MutableLiveData<ArrayList<AppOp>> by lazy {
        MutableLiveData<ArrayList<AppOp>>().also {
            initShell()
        }
    }

    private val appOpsState: MutableLiveData<Pair<AppOp, Int>> by lazy {
        MutableLiveData<Pair<AppOp, Int>>()
    }

    fun getAppOpsData(): LiveData<ArrayList<AppOp>> {
        return appOpsData
    }

    fun getAppOpsState(): LiveData<Pair<AppOp, Int>> {
        return appOpsState
    }

    fun loadAppOpsData(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ops = AppOps.getOps(context, packageInfo.packageName)
                val filtered = arrayListOf<AppOp>()

                for (op in ops) {
                    if (op.permission.lowercase().contains(keyword)) {
                        filtered.add(op)
                    }
                }

                appOpsData.postValue(filtered)
            } catch (e: ArrayIndexOutOfBoundsException) {
                postWarning(getString(R.string.not_available))
            }
        }
    }

    fun updateAppOpsState(appsOpsModel: AppOp, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.cmd(getStateChangeCommand(appsOpsModel)).exec().let {
                    if (it.isSuccess) {
                        appsOpsModel.isEnabled = appsOpsModel.isEnabled.invert()
                        appOpsState.postValue(Pair(appsOpsModel, position))
                    } else {
                        appOpsState.postValue(Pair(appsOpsModel, position))
                        postWarning("Failed to change state of ${appsOpsModel.permission}" +
                                            " : ${!appsOpsModel.isEnabled} for ${packageInfo.packageName})")
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getStateChangeCommand(appsOpsModel: AppOp): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("appops set ")
        stringBuilder.append(packageInfo.packageName)
        stringBuilder.append(" ")
        stringBuilder.append(appsOpsModel.permission + if (appsOpsModel.isEnabled) " deny" else " allow")
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
