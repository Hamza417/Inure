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
                val filtered = if (keyword.isEmpty()) ops else {
                    ops.filter { it.permission.lowercase().contains(keyword.lowercase()) } as ArrayList<AppOp>
                }
                appOpsData.postValue(filtered)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ops", e)
                postWarning(getString(R.string.not_available))
            }
        }
    }

    fun updateAppOpsState(updatedAppOp: AppOp, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val command = getStateChangeCommand(updatedAppOp)
                val success = when {
                    ConfigurationPreferences.isUsingRoot() -> Shell.cmd(command).exec().isSuccess
                    ConfigurationPreferences.isUsingShizuku() -> getShizukuService().simpleExecute(command).isSuccess
                    else -> throw IllegalStateException("No root or shizuku enabled.")
                }

                if (success) {
                    appOpsState.postValue(Pair(updatedAppOp, position))
                } else {
                    postWarning("Failed to change state: ${updatedAppOp.permission}")
                }
            }.getOrElse { postError(it) }
        }
    }

    private fun getStateChangeCommand(op: AppOp): String {
        // Extract numeric ID from OEM custom ops like MIUIOP(10008)
        val permission = if (op.permission.contains("(") && op.permission.endsWith(")")) {
            val extracted = op.permission.substringAfter("(").substringBefore(")")
            // Verify it's numeric before using it, otherwise fall back to original
            if (extracted.toIntOrNull() != null) extracted else op.permission
        } else {
            op.permission
        }

        return buildString {
            append("appops set ")
            append(AppOpScope.getCommandFlag(op.scope))
            if (op.scope == AppOpScope.UID) append(" ")
            append(packageInfo.packageName).append(" ")
            append(permission).append(" ")
            append(op.mode.value)
        }.also { Log.i(TAG, "Executing: $it") }
    }

    // TODO - the whole approach is hacky, needs a proper parser
    private fun getOps(packageName: String): ArrayList<AppOp> {
        val ops = ArrayList<AppOp>()
        val permissions = getPermissionMap() // Ensure this returns Map<String, String?>
        val rawOutput = runAndGetOutput("appops get $packageName")

        if (rawOutput.isEmpty() || rawOutput.contains("No operations.")) return ops

        val lines = rawOutput.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val seenOpsInUid = mutableSetOf<String>()
        var isCurrentlyParsingUid = false

        for (line in lines) {
            val name: String
            val data: String
            val scope: AppOpScope

            // Handle the specific "Uid mode:" header line (3 parts)
            if (line.startsWith("Uid mode:")) {
                isCurrentlyParsingUid = true
                val parts = line.split(":", limit = 3)
                // "Uid mode: COARSE_LOCATION: ignore" -> ["Uid mode", "COARSE_LOCATION", "ignore"]
                if (parts.size >= 3) {
                    name = parts[1].trim()
                    data = parts[2].trim()
                    scope = AppOpScope.UID
                } else {
                    // Malformed header, skip
                    continue
                }
            }
            // Handle standard lines (2 parts: "NAME: DATA")
            else {
                val parts = line.split(":", limit = 2)
                if (parts.size < 2) continue // Skip junk lines

                val potentialName = parts[0].trim()

                // Logic to detect if we have crossed from UID section to Package section:
                // - If we are already in Package mode, stay there.
                // - If we see Metadata (;), it's definitely Package mode.
                // - If we see a duplicate Name (already seen in UID list), it's Package mode.
                if (!isCurrentlyParsingUid || line.contains(";") || seenOpsInUid.contains(potentialName)) {
                    isCurrentlyParsingUid = false
                    name = potentialName
                    data = parts[1].trim()
                    scope = AppOpScope.PACKAGE
                } else {
                    // Still in the initial UID block
                    name = potentialName
                    data = parts[1].trim()
                    scope = AppOpScope.UID
                }
            }

            // Extract mode and metadata
            // Data looks like: "allow; time=+10d..." or just "ignore"
            val modeStr = data.split(";")[0].trim()
            val time = if (data.contains("time=")) data.substringAfter("time=").substringBefore(";") else null
            val duration = if (data.contains("duration=")) data.substringAfter("duration=").substringBefore(";") else null
            val rejectTime = if (data.contains("rejectTime=")) data.substringAfter("rejectTime=").substringBefore(";") else null

            // Construct AppOp
            // Note: permissions[name] might be null for OEM ops, handling that gracefully
            val appOp = AppOp(name, permissions[name], AppOpMode.fromString(modeStr), time, duration, rejectTime)
            appOp.scope = scope

            ops.add(appOp)

            // Track UID ops to help detect duplicates later
            if (scope == AppOpScope.UID) {
                seenOpsInUid.add(name)
            }
        }

        return ops
    }

    private fun runAndGetOutput(command: String?): String {
        return try {
            when {
                ConfigurationPreferences.isUsingRoot() -> {
                    Shell.cmd(command).exec().out.joinToString("\n")
                }
                ConfigurationPreferences.isUsingShizuku() -> {
                    getShizukuService().simpleExecute(command).output ?: ""
                }
                else -> ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Shell execution failed", e)
            ""
        }
    }

    override fun onShellCreated(shell: Shell?) {
        loadAppOpsData("")
    }

    override fun onShellDenied() { /* no-op */
    }

    override fun onShizukuCreated(shizukuServiceHelper: ShizukuServiceHelper) {
        loadAppOpsData("")
    }

    companion object {
        private const val TAG = "OperationsViewModel"
    }
}