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
                                postWarning("Failed to change state of ${updatedAppOp.permission}" +
                                                    " : ${""} for ${packageInfo.packageName}")
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
        if (op.scope == AppOpScope.UID) {
            stringBuilder.append(" ")
        }
        stringBuilder.append(packageInfo.packageName)
        stringBuilder.append(" ")

        // Extract numeric ID from OEM custom ops like MIUIOP(10008), OPPOop(20001), etc.
        // Universal pattern: if format is NAME(number), extract just the number
        val permission = if (op.permission.contains("(") && op.permission.endsWith(")")) {
            val extracted = op.permission.substringAfter("(").substringBefore(")")
            // Verify it's numeric before using it, otherwise fall back to original
            if (extracted.toIntOrNull() != null) extracted else op.permission
        } else {
            op.permission
        }

        stringBuilder.append(permission)
        stringBuilder.append(" ")
        stringBuilder.append(op.mode.value)
        Log.i(TAG, "$stringBuilder will be executed")
        return stringBuilder.toString()
    }

    private fun getOps(packageName: String): ArrayList<AppOp> {
        val ops = ArrayList<AppOp>()
        val permissions = getPermissionMap()

        // First, get all operations to extract unique operation names
        val allOpsOutput = runAndGetOutput("appops get $packageName")
        val uniqueOperations = extractUniqueOperations(allOpsOutput)

        Log.d(TAG, "Found ${uniqueOperations.size} unique operations for $packageName")

        // Query each operation individually to get clear UID and package scope separation
        for (operation in uniqueOperations) {
            // For OEM custom ops like MIUIOP(10008), extract the numeric code
            val queryOperation = if (operation.contains("(") && operation.endsWith(")")) {
                val extracted = operation.substringAfter("(").substringBefore(")")
                // Verify it's numeric, otherwise use original
                if (extracted.toIntOrNull() != null) extracted else operation
            } else {
                operation
            }

            val opOutput = runAndGetOutput("appops get $packageName $queryOperation")
            if (opOutput.isNotEmpty()) {
                parseIndividualOperation(operation, opOutput, permissions)?.let { parsedOps ->
                    ops.addAll(parsedOps)
                }
            }
        }

        return ops
    }

    /**
     * Extract unique operation names from the initial appops get output
     */
    private fun extractUniqueOperations(output: String): Set<String> {
        val operations = mutableSetOf<String>()
        val lines = output.split("\\r?\\n".toRegex())

        for (line in lines) {
            if (line.trim().isEmpty() || line == "No operations.") {
                continue
            }

            // Skip the "Uid mode:" header line
            if (line.trim().startsWith("Uid mode:")) {
                val uidLine = line.substringAfter("Uid mode:").trim()
                if (uidLine.isNotEmpty()) {
                    val opName = uidLine.split(":").firstOrNull()?.trim()
                    if (!opName.isNullOrEmpty()) {
                        operations.add(opName)
                    }
                }
                continue
            }

            // Extract operation name (everything before the first colon)
            val opName = line.split(":").firstOrNull()?.trim()
            if (!opName.isNullOrEmpty()) {
                operations.add(opName)
            }
        }

        return operations
    }

    /**
     * Parse the output of "appops get <package> <operation>"
     * This returns both UID and package scoped operations clearly separated
     *
     * Example output:
     * Uid mode: COARSE_LOCATION: foreground
     * COARSE_LOCATION: allow
     */
    private fun parseIndividualOperation(
            operation: String,
            output: String,
            permissions: Map<String, String?>
    ): List<AppOp>? {
        val ops = mutableListOf<AppOp>()
        val lines = output.split("\\r?\\n".toRegex())

        var uidModeValue: String? = null
        var packageModeValue: String? = null
        var time: String? = null
        var duration: String? = null
        var rejectTime: String? = null

        for (line in lines) {
            if (line.trim().isEmpty()) continue

            // Parse UID mode line: "Uid mode: COARSE_LOCATION: foreground"
            if (line.trim().startsWith("Uid mode:")) {
                val uidLine = line.substringAfter("Uid mode:").trim()
                val parts = uidLine.split(":".toRegex(), limit = 2)
                if (parts.size >= 2) {
                    uidModeValue = parts[1].trim().split(";").firstOrNull()?.trim()
                }
            } else {
                // Parse package mode line: "COARSE_LOCATION: allow; time=+2h3m39s662ms ago"
                val parts = line.split(":".toRegex(), limit = 2)
                if (parts.size >= 2 && parts[0].trim() == operation) {
                    val valuesPart = parts[1].trim()
                    packageModeValue = valuesPart.split(";").firstOrNull()?.trim()

                    // Extract metadata
                    if (valuesPart.contains("time=")) {
                        time = valuesPart.split("time=").getOrNull(1)
                            ?.split(";")?.firstOrNull()?.trim()
                    }

                    if (valuesPart.contains("duration=")) {
                        duration = valuesPart.split("duration=").getOrNull(1)
                            ?.split(";")?.firstOrNull()?.trim()
                    }

                    if (valuesPart.contains("rejectTime=")) {
                        rejectTime = valuesPart.split("rejectTime=").getOrNull(1)
                            ?.split(";")?.firstOrNull()?.trim()
                    }
                }
            }
        }

        val id = permissions[operation]

        // Add UID-scoped operation if present
        if (uidModeValue != null) {
            val uidOp = AppOp(operation, id, AppOpMode.fromString(uidModeValue), null, null, null)
            uidOp.scope = AppOpScope.UID
            ops.add(uidOp)
            Log.d(TAG, "UID scope: $operation -> $uidModeValue")
        }

        // Add package-scoped operation if present
        if (packageModeValue != null) {
            val packageOp = AppOp(operation, id, AppOpMode.fromString(packageModeValue), time, duration, rejectTime)
            packageOp.scope = AppOpScope.PACKAGE
            ops.add(packageOp)
            Log.d(TAG, "Package scope: $operation -> $packageModeValue")
        }

        return ops.ifEmpty { null }
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
