package app.simple.inure.util

import android.content.Context

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
}