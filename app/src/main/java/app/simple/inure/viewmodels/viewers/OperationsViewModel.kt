package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionMap
import app.simple.inure.enums.AppOpMode
import app.simple.inure.enums.AppOpScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.AppOp
import app.simple.inure.preferences.ConfigurationPreferences
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
                val ops = getOps(packageInfo.packageName)
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

    fun updateAppOpsState(updatedAppOp: AppOp, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                when {
                    ConfigurationPreferences.isUsingRoot() -> {
                        Shell.cmd(getStateChangeCommand(updatedAppOp)).exec().let {
                            if (it.isSuccess) {
                                appOpsState.postValue(Pair(updatedAppOp, position))
                            } else {
                                appOpsState.postValue(Pair(updatedAppOp, position))
                                postWarning("Failed to change state of ${updatedAppOp.permission}" +
                                                    " : ${""} for ${packageInfo.packageName})")
                            }
                        }
                    }
                    ConfigurationPreferences.isUsingShizuku() -> {
                        getShizukuService().simpleExecute(getStateChangeCommand(updatedAppOp)).let {
                            if (it.isSuccess) {
                                appOpsState.postValue(Pair(updatedAppOp, position))
                            } else {
                                appOpsState.postValue(Pair(updatedAppOp, position))
                                postWarning("Failed to change state of ${updatedAppOp.permission}" +
                                                    " : ${""} for ${packageInfo.packageName})")
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

    private fun getStateChangeCommand(op: AppOp): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("appops set ")
        stringBuilder.append(AppOpScope.getCommandFlag(op.scope))
        stringBuilder.append(" ")
        stringBuilder.append(packageInfo.packageName)
        stringBuilder.append(" ")
        stringBuilder.append(op.permission)
        stringBuilder.append(" ")
        stringBuilder.append(op.mode.value)
        Log.i(TAG, "$stringBuilder will be executed")
        return stringBuilder.toString()
    }

    private fun getOps(packageName: String): ArrayList<AppOp> {
        val ops = ArrayList<AppOp>()
        val permissions = getPermissionMap()

        for (line in runAndGetOutput("appops get $packageName").split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val splitOp = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val name = splitOp[0].trim { it <= ' ' }

            if (line == "No operations.") {
                continue
            }

            // Handle UID scoped app ops lines like: "Uid mode: COARSE_LOCATION: ignore"
            if (name == "Uid mode") {
                if (splitOp.size >= 3) {
                    val uidPermission = splitOp[1].trim { it <= ' ' }
                    val uidModeStr = splitOp[2].trim { it <= ' ' }
                    val uidId = permissions[uidPermission]
                    val uidAppOp = AppOp(uidPermission, uidId, AppOpMode.fromString(uidModeStr), null, null, null)
                    uidAppOp.scope = AppOpScope.UID
                    ops.add(uidAppOp)
                } else if (splitOp.size >= 2) {
                    // Fallback in case format is "Uid mode: COARSE_LOCATION" without mode (unlikely)
                    val uidPermissionAndMode = splitOp[1].trim { it <= ' ' }
                    val inner = uidPermissionAndMode.split(":".toRegex()).toTypedArray()
                    if (inner.size >= 2) {
                        val uidPermission = inner[0].trim { it <= ' ' }
                        val uidModeStr = inner[1].trim { it <= ' ' }
                        val uidId = permissions[uidPermission]
                        val uidAppOp = AppOp(uidPermission, uidId, AppOpMode.fromString(uidModeStr), null, null, null)
                        uidAppOp.scope = AppOpScope.UID
                        ops.add(uidAppOp)
                    }
                }

                // Skip to next line after handling UID scoped op
                continue
            }

            // Parse regular package-scoped ops
            val mode = splitOp[1].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
            var time: String? = null
            var duration: String? = null
            var rejectTime: String? = null
            val id = permissions[name]

            if (splitOp[1].contains("time=")) {
                time = splitOp[1].split("time=".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].split(";".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0].trim { it <= ' ' }
            }

            if (splitOp[1].contains("duration=")) {
                duration = splitOp[1].split("duration=".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].split(";".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0].trim { it <= ' ' }
            }

            if (splitOp[1].contains("rejectTime=")) {
                rejectTime = splitOp[1].split("rejectTime=".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].trim { it <= ' ' }
            }

            val appOp = AppOp(name, id, AppOpMode.fromString(mode), time, duration, rejectTime)
            appOp.scope = AppOpScope.PACKAGE
            ops.add(appOp)
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
        } catch (_: Exception) {
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

    companion object {
        private const val TAG = "OperationsViewModel"
    }
}
