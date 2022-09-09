package app.simple.inure.apk.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import app.simple.inure.util.PermissionUtils.areStoragePermissionsGranted
import java.io.File

object PackageData {
    fun makePackageFolder(context: Context) {
        if (getPackageDir((context))!!.exists() && getPackageDir((context))!!.isFile) {
            getPackageDir((context))!!.delete()
        }
        getPackageDir((context))!!.mkdirs()
    }

    fun getPackageDir(context: Context): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !context.areStoragePermissionsGranted()) {
            context.getExternalFilesDir("")
        } else {
            File(Environment.getExternalStorageDirectory(), "Inure App Manager")
        }
    }

    fun Context.getInstallerDir(name: String): File {
        if (File(cacheDir.path + "/installer_cache/").exists() &&
            File(cacheDir.path + "/installer_cache/").isFile) {
            File(cacheDir.path + "/installer_cache/").delete()
        }
        File(cacheDir.path + "/installer_cache/").mkdir()
        return File(cacheDir.path + "/installer_cache/" + name)
    }

    fun Context.getInstallerDir(name: String, dirName: String): File {
        if (File(cacheDir.path + "/installer_cache/" + dirName + "/").exists() &&
            File(cacheDir.path + "/installer_cache/" + dirName + "/").isFile) {
            File(cacheDir.path + "/installer_cache/" + dirName + "/").delete()
        }
        File(cacheDir.path + "/installer_cache/" + dirName + "/").mkdir()
        return File(cacheDir.path + "/installer_cache/" + dirName + "/" + name)
    }
}