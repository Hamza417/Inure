package app.simple.inure.apk.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.PermissionUtils.areStoragePermissionsGranted
import app.simple.inure.util.SDCard
import java.io.File

object PackageData {
    fun makePackageFolder(context: Context, path: String) {
        if (getPackageDir((context), path)!!.exists() && getPackageDir((context), path)!!.isFile) {
            getPackageDir((context), path)!!.delete()
        }
        getPackageDir((context), path)!!.mkdirs()
    }

    fun makePackageFolder(context: Context) {
        if (getPackageDir((context))!!.exists() && getPackageDir((context))!!.isFile) {
            getPackageDir((context))!!.delete()
        }
        getPackageDir((context))!!.mkdirs()
    }

    fun getPackageDir(context: Context): File? {
        return getPackageDir(context, ConfigurationPreferences.getAppPath())
    }

    fun getPackageDir(context: Context, path: String): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !context.areStoragePermissionsGranted()) {
            if (ConfigurationPreferences.isExternalStorage()) {
                (SDCard.findSdCardPath(context).absolutePath + "/" + path).toFile()
            } else {
                context.getExternalFilesDir("")
            }
        } else {
            if (ConfigurationPreferences.isExternalStorage()) {
                (SDCard.findSdCardPath(context).absolutePath + "/" + path).toFile()
            } else {
                File(Environment.getExternalStorageDirectory(), path)
            }
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

    fun Context.getOtherCacheDir(name: String): File {
        if (File(cacheDir.path + "/other_cache/").exists() &&
            File(cacheDir.path + "/other_cache/").isFile) {
            File(cacheDir.path + "/other_cache/").delete()
        }
        File(cacheDir.path + "/other_cache/").mkdir()
        return File(cacheDir.path + "/other_cache/" + name)
    }

    fun Context.getInstallerDir(name: String, dirName: String): File {
        if (File(cacheDir.path + "/installer_cache/" + dirName + "/").exists() &&
            File(cacheDir.path + "/installer_cache/" + dirName + "/").isFile) {
            File(cacheDir.path + "/installer_cache/" + dirName + "/").delete()
        }
        File(cacheDir.path + "/installer_cache/" + dirName + "/").mkdir()
        return File(cacheDir.path + "/installer_cache/" + dirName + "/" + name)
    }

    fun Context.getCachedDir(name: String): File {
        if (File(cacheDir.path + "/cached/").exists() &&
            File(cacheDir.path + "/cached/").isFile) {
            File(cacheDir.path + "/cached/").delete()
        }
        File(cacheDir.path + "/cached/").mkdir()
        return File(cacheDir.path + "/cached/" + name)
    }

    fun Context.getCachedDir(name: String, dirName: String): File {
        if (File(cacheDir.path + "/cached/" + dirName + "/").exists() &&
            File(cacheDir.path + "/cached/" + dirName + "/").isFile) {
            File(cacheDir.path + "/cached/" + dirName + "/").delete()
        }
        File(cacheDir.path + "/cached/" + dirName + "/").mkdir()
        return File(cacheDir.path + "/cached/" + dirName + "/" + name)
    }
}