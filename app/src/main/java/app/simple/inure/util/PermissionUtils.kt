package app.simple.inure.util

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.core.app.AppOpsManagerCompat

object PermissionUtils {
    fun Context.arePermissionsGranted(uriString: String?): Boolean {
        if (uriString.isNullOrEmpty()) return false
        // list of all persisted permissions for the app
        for (i in contentResolver.persistedUriPermissions) {
            if (i.uri.toString() == uriString && i.isWritePermission && i.isReadPermission) {
                return true
            }
        }
        return false
    }

    fun Context.checkForUsageAccessPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        }

        return mode == AppOpsManagerCompat.MODE_ALLOWED
    }
}