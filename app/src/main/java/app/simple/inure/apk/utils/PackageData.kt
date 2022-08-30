package app.simple.inure.apk.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object PackageData {

    fun makePackageFolder(context: Context) {
        if (getPackageDir((context))!!.exists() && getPackageDir((context))!!.isFile()) {
            getPackageDir((context))!!.delete()
        }
        getPackageDir((context))!!.mkdirs()
    }

    fun getPackageDir(context: Context): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getExternalFilesDir("")
        } else {
            File(Environment.getExternalStorageDirectory(), "Inure App Manager")
        }
    }
}