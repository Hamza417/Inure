package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionMap
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.AppOp
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.invert
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsViewModel(application: Application, val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val appOpsData: MutableLiveData<ArrayList<AppOp>> by lazy {
        MutableLiveData<ArrayList<AppOp>>().also {
            initializeCoreFramework()
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
                val ops = getOps(context, packageInfo.packageName)
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
                when {
                    ConfigurationPreferences.isUsingRoot() -> {
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
                    }
                    ConfigurationPreferences.isUsingShizuku() -> {
                        getShizukuService().simpleExecute(getStateChangeCommand(appsOpsModel)).let {
                            if (it.isSuccess) {
                                appsOpsModel.isEnabled = appsOpsModel.isEnabled.invert()
                                appOpsState.postValue(Pair(appsOpsModel, position))
                            } else {
                                appOpsState.postValue(Pair(appsOpsModel, position))
                                postWarning("Failed to change state of ${appsOpsModel.permission}" +
                                                    " : ${!appsOpsModel.isEnabled} for ${packageInfo.packageName})")
                            }
                        }
                    }
                    else -> {
                        // This should be unreachable
                        throw IllegalStateException("No root or shizuku, please enable one of them to use this feature.")
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

    private fun getOps(context: Context?, packageName: String): java.util.ArrayList<AppOp> {
        val ops = java.util.ArrayList<AppOp>()
        val permissions = getPermissionMap(context!!)
        for (line in runAndGetOutput("appops get $packageName").split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val splitOp = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val name = splitOp[0].trim { it <= ' ' }
            if (line != "No operations." && name != "Uid mode") {
                val mode = splitOp[1].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
                var time: String? = null
                var duration: String? = null
                var rejectTime: String? = null
                val id = permissions[name]
                if (splitOp[1].contains("time=")) {
                    time = splitOp[1].split("time=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
                }
                if (splitOp[1].contains("duration=")) {
                    duration = splitOp[1].split("duration=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
                }
                if (splitOp[1].contains("rejectTime=")) {
                    rejectTime = splitOp[1].split("rejectTime=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
                }
                ops.add(AppOp(name, id, mode == "allow", time, duration, rejectTime))
            }
        }
        return ops
    }

    private fun runAndGetOutput(command: String?): String {
        val sb = java.lang.StringBuilder()
        return try {
            when {
                ConfigurationPreferences.isUsingRoot() -> {
                    val outputs = Shell.cmd(command).exec().out
                    if (ShellUtils.isValidOutput(outputs)) {
                        for (output in outputs) {
                            Log.d("AppOp -> ", output!!)
                            sb.append(output).append("\n")
                        }
                    }
                }
                ConfigurationPreferences.isUsingShizuku() -> {
                    val outputs = getShizukuService().simpleExecute(command).output?.split("\n".toRegex())?.toTypedArray()
                    if (outputs?.isNotEmpty() == true) {
                        for (output in outputs) {
                            Log.d("AppOp -> ", output)
                            sb.append(output).append("\n")
                        }
                    }
                }
                else -> {
                    // This should be unreachable
                    throw IllegalStateException("No root or shizuku, please enable one of them to use this feature.")
                }
            }

            sb.trim().toString()
        } catch (e: Exception) {
            ""
        }
    }

    override fun onShellCreated(shell: Shell?) {
        loadAppOpsData("")
    }

    override fun onShellDenied() {
        /* no-op */
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        loadAppOpsData("")
    }
}
