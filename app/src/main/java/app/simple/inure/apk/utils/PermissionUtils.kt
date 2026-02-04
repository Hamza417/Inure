package app.simple.inure.apk.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import app.simple.inure.R
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.StringUtils.appendFlag
import java.io.BufferedReader
import java.io.InputStreamReader

object PermissionUtils {

    // MIUI custom app ops with their numeric IDs and descriptions
    private val MIUI_APP_OPS = mapOf(
            10001 to "Allows the app to change WiFi state.",
            10002 to "Allows the app to change Bluetooth state.",
            10003 to "Allows the app to change mobile data connection state.",
            10004 to "Allows the app to send MMS messages.",
            10005 to "Allows the app to read MMS messages.",
            10006 to "Allows the app to write/modify MMS messages.",
            10007 to "Allows the app to start automatically when device boots.",
            10008 to "Allows the app to start automatically in the background.",
            10009 to "Allows the app to change NFC state.",
            10010 to "Allows the app to delete SMS messages.",
            10011 to "Allows the app to delete MMS messages.",
            10012 to "Allows the app to delete contacts.",
            10013 to "Allows the app to delete call logs.",
            10014 to "Allows the app to schedule exact alarms.",
            10015 to "Allows the app to access Xiaomi account information.",
            10016 to "Allows the app to use NFC functionality.",
            10017 to "Allows the app to install shortcuts on the home screen.",
            10018 to "Allows the app to read notification SMS messages.",
            10019 to "Allows the app to get the list of running tasks.",
            10020 to "Allows the app to show content when the device is locked.",
            10021 to "Allows the app to start activities from the background.",
            10022 to "Allows the app to get the list of installed apps.",
            10023 to "Allows the app to run foreground services"
    )

    private const val PROTECTION_FLAG_OEM = 0x4000
    private const val PROTECTION_FLAG_VENDOR_PRIVILEGED = 0x8000
    private const val PROTECTION_FLAG_SYSTEM_TEXT_CLASSIFIER = 0x10000
    private const val PROTECTION_FLAG_WELLBEING = 0x20000
    private const val PROTECTION_FLAG_DOCUMENTER = 0x40000
    private const val PROTECTION_FLAG_CONFIGURATOR = 0x80000
    private const val PROTECTION_FLAG_INCIDENT_REPORT_APPROVER = 0x100000
    private const val PROTECTION_FLAG_APP_PREDICTOR = 0x200000
    private const val PROTECTION_FLAG_RETAIL_DEMO = 0x1000000

    private const val FLAG_PERMISSION_USER_SET: Int = 1 shl 0 // 1
    private const val FLAG_PERMISSION_USER_FIXED: Int = 1 shl 1 // 2
    private const val FLAG_PERMISSION_POLICY_FIXED: Int = 1 shl 2 // 4
    private const val FLAG_PERMISSION_REVOKE_ON_UPGRADE: Int = 1 shl 3 // 8
    private const val FLAG_PERMISSION_SYSTEM_FIXED: Int = 1 shl 4 // 16
    private const val FLAG_PERMISSION_GRANTED_BY_DEFAULT: Int = 1 shl 5 // 32

    private val exceptionPermissions = arrayOf(
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.DUMP",
            "android.permission.READ_LOGS",
            "android.permission.READ_FRAME_BUFFER",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.INTERACT_ACROSS_USERS",
    )

    fun String.getPermissionInfo(context: Context): PermissionInfo? {
        try {
            return context.packageManager.getPermissionInfo(this, PackageManager.GET_META_DATA)
        } catch (_: PackageManager.NameNotFoundException) {
            // e.printStackTrace()
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun isDangerous(permissionInfo: PermissionInfo): Boolean {
        val isDangerous = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (permissionInfo.protection) {
                PermissionInfo.PROTECTION_DANGEROUS -> true
                PermissionInfo.PROTECTION_SIGNATURE -> false
                PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM -> false
                else -> false
            }
        } else {
            when (permissionInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) {
                PermissionInfo.PROTECTION_DANGEROUS -> true
                PermissionInfo.PROTECTION_SIGNATURE -> false
                PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM -> false
                else -> false
            }
        }

        val isIrrevocable = (permissionInfo.flags and (FLAG_PERMISSION_POLICY_FIXED or FLAG_PERMISSION_SYSTEM_FIXED)) != 0

        return isDangerous && !isIrrevocable
    }

    fun getPermissionMap(): HashMap<String, String> {
        val bufferedReader = BufferedReader(
                InputStreamReader(PermissionUtils::class.java.getResourceAsStream("/permissions.txt")))
        val permissionList = bufferedReader.readLines()
        val map = HashMap<String, String>()

        permissionList.forEach {
            val shortName = it.substring(it.lastIndexOf(".") + 1)
            map[shortName] = it
        }

        return map
    }

    fun getAndroidPermissionList(): List<String> {
        val bufferedReader = BufferedReader(
                InputStreamReader(PermissionUtils::class.java.getResourceAsStream("/permissions.txt")))
        return bufferedReader.readLines().toArrayList().sorted()
    }

    fun PermissionInfo.isException(): Boolean {
        return name in exceptionPermissions
    }

    @Suppress("deprecation")
    fun createPermissionFlags(permissionInfo: PermissionInfo, isGranted: Int, isInstalled: Boolean, context: Context): String {
        val sb = buildString {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                when (permissionInfo.protection and PermissionInfo.PROTECTION_MASK_BASE) {
                    PermissionInfo.PROTECTION_DANGEROUS -> {
                        appendFlag(context.getString(R.string.dangerous))
                    }
                    PermissionInfo.PROTECTION_NORMAL -> {
                        appendFlag(context.getString(R.string.normal))
                    }
                    PermissionInfo.PROTECTION_SIGNATURE -> {
                        appendFlag(context.getString(R.string.signature))
                    }
                    PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM -> {
                        appendFlag(context.getString(R.string.signature_or_system))
                    }
                }

                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_PRIVILEGED != 0) {
                    appendFlag(context.getString(R.string.privileged))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_DEVELOPMENT != 0) {
                    appendFlag(context.getString(R.string.development))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_APPOP != 0) {
                    appendFlag("AppOp")
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_PRE23 != 0) {
                    appendFlag("Pre23")
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_INSTALLER != 0) {
                    appendFlag(context.getString(R.string.installer))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_VERIFIER != 0) {
                    appendFlag(context.getString(R.string.verifier))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_PREINSTALLED != 0) {
                    appendFlag(context.getString(R.string.pre_installed))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_SETUP != 0) {
                    appendFlag(context.getString(R.string.setup))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_INSTANT != 0) {
                    appendFlag(context.getString(R.string.instant))
                }
                if (permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_RUNTIME_ONLY != 0) {
                    appendFlag(context.getString(R.string.runtime))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_OEM != 0) {
                    appendFlag(context.getString(R.string.oem))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_VENDOR_PRIVILEGED != 0) {
                    appendFlag(context.getString(R.string.vendor_privileged))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_SYSTEM_TEXT_CLASSIFIER != 0) {
                    appendFlag(context.getString(R.string.text_classifier))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_WELLBEING != 0) {
                    appendFlag(context.getString(R.string.wellbeing))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_DOCUMENTER != 0) {
                    appendFlag(context.getString(R.string.documenter))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_CONFIGURATOR != 0) {
                    appendFlag(context.getString(R.string.configurator))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_INCIDENT_REPORT_APPROVER != 0) {
                    appendFlag(context.getString(R.string.incident_report_approver))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_APP_PREDICTOR != 0) {
                    appendFlag(context.getString(R.string.app_predictor))
                }
                if (permissionInfo.protectionFlags and PROTECTION_FLAG_RETAIL_DEMO != 0) {
                    appendFlag(context.getString(R.string.retail_demo))
                }
            } else {
                when (permissionInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) {
                    PermissionInfo.PROTECTION_DANGEROUS -> {
                        appendFlag(context.getString(R.string.dangerous))
                    }
                    PermissionInfo.PROTECTION_NORMAL -> {
                        appendFlag(context.getString(R.string.normal))
                    }
                    PermissionInfo.PROTECTION_SIGNATURE -> {
                        appendFlag(context.getString(R.string.signature))
                    }
                    PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM -> {
                        appendFlag(context.getString(R.string.signature_or_system))
                    }
                }
            }

            if (permissionInfo.flags and FLAG_PERMISSION_POLICY_FIXED != 0) {
                appendFlag(context.getString(R.string.policy_fixed))
            }
            if (permissionInfo.flags and FLAG_PERMISSION_USER_FIXED != 0) {
                appendFlag(context.getString(R.string.user_fixed))
            }
            if (permissionInfo.flags and FLAG_PERMISSION_REVOKE_ON_UPGRADE != 0) {
                appendFlag(context.getString(R.string.revoke_on_upgrade))
            }
            if (permissionInfo.flags and FLAG_PERMISSION_SYSTEM_FIXED != 0) {
                appendFlag(context.getString(R.string.system_fixed))
            }
            if (permissionInfo.flags and FLAG_PERMISSION_GRANTED_BY_DEFAULT != 0) {
                appendFlag(context.getString(R.string.granted_by_default))
            }
            if (permissionInfo.flags and FLAG_PERMISSION_USER_SET != 0) {
                appendFlag(context.getString(R.string.user_set))
            }

            if (isInstalled) {
                when (isGranted) {
                    0 -> {
                        appendFlag(context.getString(R.string.rejected))
                    }
                    1 -> {
                        appendFlag(context.getString(R.string.granted))
                    }
                    2 -> {
                        appendFlag(context.getString(R.string.unknown))
                    }
                    else -> {
                        appendFlag(context.getString(R.string.unknown))
                    }
                }
            }
        }

        return sb
    }

    /**
     * Gets the description for MIUI custom app ops based on the numeric ID
     * @param opId The numeric ID (e.g., 10008 from MIUIOP(10008))
     * @return Description of the operation or null if not found
     */
    fun getMiuiAppOpDescription(opId: Int): String? {
        return MIUI_APP_OPS[opId]
    }

    /**
     * Gets the description for MIUI custom app ops from the full op name
     * @param opName The full operation name (e.g., "MIUIOP(10008)")
     * @return Description of the operation or null if not found or not a MIUI op
     */
    fun getMiuiAppOpDescription(opName: String?): String? {
        opName?.let {
            if (it.contains("(") && it.endsWith(")")) {
                val numericId = it.substringAfter("(").substringBefore(")").toIntOrNull()
                return numericId?.let { id -> MIUI_APP_OPS[id] }
            }
        }
        return null
    }

    fun Context.getPermissionDescription(name: String?): String {
        // First check if it's a MIUI custom op (e.g., MIUIOP(10008))
        name?.let {
            if (it.contains("(") && it.endsWith(")")) {
                val numericId = it.substringAfter("(").substringBefore(")").toIntOrNull()
                numericId?.let { id ->
                    MIUI_APP_OPS[id]?.let { description ->
                        return description
                    }
                }
            }
        }

        // Fall back to standard Android permission description
        kotlin.runCatching {
            val desc = name!!.getPermissionInfo(this)!!.loadDescription(packageManager)

            return if (desc.isNullOrEmpty()) {
                getString(R.string.desc_not_available)
            } else {
                desc.toString()
            }
        }.getOrElse {
            return getString(R.string.desc_not_available)
        }
    }
}
