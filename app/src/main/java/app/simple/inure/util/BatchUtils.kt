package app.simple.inure.util

import android.content.Context
import android.content.pm.PackageInfo
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.constants.Misc

object BatchUtils {

    fun Context.getBundlePathAndFileName(packageInfo: PackageInfo): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(PackageData.getPackageDir(this))
        stringBuilder.append("/")
        stringBuilder.append(packageInfo.applicationInfo.name)
        stringBuilder.append("_(${packageInfo.versionName})")
        stringBuilder.append(Misc.splitApkFormat)
        return stringBuilder.toString()
    }

    fun getApkPathAndFileName(packageInfo: PackageInfo): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(packageInfo.applicationInfo.name)
        stringBuilder.append("_(${packageInfo.versionName})")
        stringBuilder.append(".apk")
        return stringBuilder.toString()
    }
}