package app.simple.inure.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import app.simple.inure.R
import java.io.File

object PackageUtils {
    /**
     * Fetches the app's name from the package id of the same application
     * @param context of the given environment
     * @param applicationInfo is [ApplicationInfo] object containing app's
     *        information
     * @return app's name as [String]
     */
    fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String? {
        return try {
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.package_unknown)
        }
    }

    fun ApplicationInfo.launchThisPackage(activity: Activity) {
        try {
            val intent = activity.packageManager.getLaunchIntentForPackage(this.packageName)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        } catch (e: NullPointerException) {
            Toast.makeText(activity.baseContext, "No activity to be launched", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * this function kills an app using app's package id as
     * identifier, system apps will not be killed.
     */
    fun ApplicationInfo.killThisApp(activity: Activity) {
        val mActivityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (this.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
            Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.warning_kill_system_app), Toast.LENGTH_SHORT).show()
        } else {
            mActivityManager.killBackgroundProcesses(this.packageName)
            Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.alert_app_killed), Toast.LENGTH_SHORT).show()
        }
    }

    fun ApplicationInfo.uninstallThisPackage(context: Context) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:${this.packageName}")
        context.startActivity(intent)
    }

    /**
     * Fetches the directory size of this installed application
     * @return [Long] and should be formatted manually
     */
    fun ApplicationInfo.getPackageSize(): Long {
        return File(this.publicSourceDir).length()
    }
}