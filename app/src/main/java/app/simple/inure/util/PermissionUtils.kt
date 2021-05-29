package app.simple.inure.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.Environment
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import app.simple.inure.R


object PermissionUtils {

    private const val PROTECTION_FLAG_OEM = 0x4000
    private const val PROTECTION_FLAG_VENDOR_PRIVILEGED = 0x8000
    private const val PROTECTION_FLAG_SYSTEM_TEXT_CLASSIFIER = 0x10000
    private const val PROTECTION_FLAG_WELLBEING = 0x20000
    private const val PROTECTION_FLAG_DOCUMENTER = 0x40000
    private const val PROTECTION_FLAG_CONFIGURATOR = 0x80000
    private const val PROTECTION_FLAG_INCIDENT_REPORT_APPROVER = 0x100000
    private const val PROTECTION_FLAG_APP_PREDICTOR = 0x200000
    private const val PROTECTION_FLAG_RETAIL_DEMO = 0x1000000

    @Nullable
    fun String.getPermissionInfo(context: Context): PermissionInfo? {
        try {
            return context.packageManager.getPermissionInfo(this, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @Suppress("deprecation")
    fun protectionToString(level: Int, context: Context): String {

        var protectionLevel = "????"

        when (level and PermissionInfo.PROTECTION_MASK_BASE) {
            PermissionInfo.PROTECTION_DANGEROUS -> protectionLevel = context.getString(R.string.dangerous)
            PermissionInfo.PROTECTION_NORMAL -> protectionLevel = context.getString(R.string.normal)
            PermissionInfo.PROTECTION_SIGNATURE -> protectionLevel = context.getString(R.string.signature)
            PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM -> protectionLevel = context.getString(R.string.signature_or_system)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_PRIVILEGED != 0) {
            protectionLevel += " | " + context.getString(R.string.privileged)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_DEVELOPMENT != 0) {
            protectionLevel += " | " + context.getString(R.string.development)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_APPOP != 0) {
            protectionLevel += " | AppOp"
        }
        if (level and PermissionInfo.PROTECTION_FLAG_PRE23 != 0) {
            protectionLevel += " | Pre23"
        }
        if (level and PermissionInfo.PROTECTION_FLAG_INSTALLER != 0) {
            protectionLevel += " | " + context.getString(R.string.installer)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_VERIFIER != 0) {
            protectionLevel += " | " + context.getString(R.string.verifier)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_PREINSTALLED != 0) {
            protectionLevel += " | " + context.getString(R.string.pre_installed)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_SETUP != 0) {
            protectionLevel += " | " + context.getString(R.string.setup)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_INSTANT != 0) {
            protectionLevel += " | " + context.getString(R.string.instant)
        }
        if (level and PermissionInfo.PROTECTION_FLAG_RUNTIME_ONLY != 0) {
            protectionLevel += " | " + context.getString(R.string.runtime)
        }
        if (level and PROTECTION_FLAG_OEM != 0) {
            protectionLevel += " | " + context.getString(R.string.oem)
        }
        if (level and PROTECTION_FLAG_VENDOR_PRIVILEGED != 0) {
            protectionLevel += " | " + context.getString(R.string.vendor_privileged)
        }
        if (level and PROTECTION_FLAG_SYSTEM_TEXT_CLASSIFIER != 0) {
            protectionLevel += " | " + context.getString(R.string.text_classifier)
        }
        if (level and PROTECTION_FLAG_WELLBEING != 0) {
            protectionLevel += " | " + context.getString(R.string.wellbeing)
        }
        if (level and PROTECTION_FLAG_DOCUMENTER != 0) {
            protectionLevel += " | " + context.getString(R.string.documenter)
        }
        if (level and PROTECTION_FLAG_CONFIGURATOR != 0) {
            protectionLevel += " | " + context.getString(R.string.configurator)
        }
        if (level and PROTECTION_FLAG_INCIDENT_REPORT_APPROVER != 0) {
            protectionLevel += " | " + context.getString(R.string.incident_report_approver)
        }
        if (level and PROTECTION_FLAG_APP_PREDICTOR != 0) {
            protectionLevel += " | " + context.getString(R.string.app_predictor)
        }
        if (level and PROTECTION_FLAG_RETAIL_DEMO != 0) {
            protectionLevel += " | " + context.getString(R.string.retail_demo)
        }
        return protectionLevel
    }

    fun hasPermission(context: Context?, permissionName: String?): Boolean {
        return ContextCompat.checkSelfPermission(context!!, permissionName!!) == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
