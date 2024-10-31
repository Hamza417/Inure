package app.simple.inure.util

import android.content.Context
import android.content.pm.PackageInfo
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Misc

object BatchUtils {
    fun Context.getBundlePathAndFileName(packageInfo: PackageInfo): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(PackageData.getPackageDir(this))
        stringBuilder.append("/")
        stringBuilder.append(packageInfo.safeApplicationInfo.name.sanitize())
        stringBuilder.append("_(${packageInfo.versionName?.sanitize()})")
        stringBuilder.append(Misc.splitApkFormat)
        return stringBuilder.toString()
    }

    fun getApkPathAndFileName(packageInfo: PackageInfo): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(packageInfo.safeApplicationInfo.name.sanitize())
        stringBuilder.append("_(${packageInfo.versionName?.sanitize()})")
        stringBuilder.append(".apk")
        return stringBuilder.toString()
    }

    private fun String.sanitize(): String {
        return replace(Regex("[^\\p{L}\\p{N}._-]"), "_")
    }
}
