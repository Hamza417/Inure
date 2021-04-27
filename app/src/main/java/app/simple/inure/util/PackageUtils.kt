package app.simple.inure.util

import android.app.Activity
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.IPackageStatsObserver
import android.content.pm.PackageManager
import android.content.pm.PackageStats
import android.net.Uri
import android.os.RemoteException
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import app.simple.inure.R
import app.simple.inure.model.PackageSizes
import java.lang.reflect.Method


object  PackageUtils {

    const val UNINSTALL_REQUEST_CODE = 6452

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
            context.getString(R.string.unknown)
        }
    }

    /**
     * Fetches the app's version name from the package id of the application
     * @param context of the given environment
     * @param applicationInfo is [ApplicationInfo] object containing app's
     *        information
     * @return app's version name as [String]
     */
    fun getApplicationVersion(context: Context, applicationInfo: ApplicationInfo): String {
        return try {
            context.packageManager.getPackageInfo(applicationInfo.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        } catch (e: NullPointerException) {
            context.getString(R.string.unknown)
        }
    }

    /**
     * Fetches the app's version code from the package id of the application
     * @param context of the given environment
     * @param applicationInfo is [ApplicationInfo] object containing app's
     *        information
     * @return app's version code as [String]
     */
    fun getApplicationVersionCode(context: Context, applicationInfo: ApplicationInfo): String {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(applicationInfo.packageName, 0).longVersionCode.toString()
            } else {
                @Suppress("deprecation")
                context.packageManager.getPackageInfo(applicationInfo.packageName, 0).versionCode.toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }

    /**
     * Fetches the app's install date from the package id of the application
     * @param context of the given environment
     * @return app's install date as [String]
     */
    fun ApplicationInfo.getApplicationInstallTime(context: Context): String {
        return try {
            DateUtils.formatDate(context.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime)
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }

    /**
     * Fetches the app's last update from the package id of the application
     * @param context of the given environment
     *
     * @return app's last update date as [String]
     */
    fun ApplicationInfo.getApplicationLastUpdateTime(context: Context): String {
        return try {
            DateUtils.formatDate(context.packageManager.getPackageInfo(this.packageName, 0).lastUpdateTime)
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }

    fun ApplicationInfo.launchThisPackage(activity: Activity) {
        try {
            val intent = activity.packageManager.getLaunchIntentForPackage(this.packageName)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        } catch (e: NullPointerException) {
            Toast.makeText(activity.baseContext, "No activity to be launched", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * this function kills an app using app's package id as
     * identifier, system apps will not be killed.
     */
    fun ApplicationInfo.killThisApp(activity: Activity) {
        val mActivityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (this.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
            Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.warning_kill_system_app), Toast.LENGTH_SHORT)
                    .show()
        } else {
            mActivityManager.killBackgroundProcesses(this.packageName)
            Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.alert_app_killed), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * Passes uninstall command to system [PackageManager] ad attaches
     * a request code [UNINSTALL_REQUEST_CODE] which can be used to
     * observe the results later on in the activity and update the list
     * accordingly using [Activity.onActivityResult] listener
     *
     * @param appUninstallObserver reference of the current [ActivityResultLauncher]
     */
    @Suppress("deprecation")
    fun ApplicationInfo.uninstallThisPackage(appUninstallObserver: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        intent.data = Uri.parse("package:${this.packageName}")
        appUninstallObserver.launch(intent)
    }

    /**
     * Fetches the directory size of this installed application
     * @return [Long] and should be formatted manually
     */
    fun ApplicationInfo.getPackageSize(context: Context): PackageSizes {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            return try {
                val storageStats = storageStatsManager.queryStatsForUid(this.storageUuid, this.uid)
                val cacheSize = storageStats.cacheBytes
                val dataSize = storageStats.dataBytes
                val apkSize = storageStats.appBytes
                PackageSizes(dataSize = dataSize,
                             cacheSize = cacheSize,
                             codeSize = apkSize)
            } catch (e: Exception) {
                PackageSizes() // Empty data with 0 as values
            }
        } else {
            var packageSizes = PackageSizes()
            val packageManager: PackageManager = context.packageManager
            val getPackageSizeInfo: Method = packageManager.javaClass.getMethod("getPackageSizeInfo", String::class.java, IPackageStatsObserver::class.java)

            @Suppress("deprecation")
            getPackageSizeInfo.invoke(packageManager, this.packageName, object : IPackageStatsObserver.Stub() {
                @Throws(RemoteException::class)
                override fun onGetStatsCompleted(packageStats: PackageStats, succeeded: Boolean) {
                    if (succeeded) {
                        packageSizes = PackageSizes(
                            dataSize = packageStats.dataSize,
                            cacheSize = packageStats.cacheSize,
                            codeSize = packageStats.codeSize,
                            externalDataSize = packageStats.externalDataSize,
                            externalCacheSize = packageStats.externalCacheSize,
                            externalCodeSize = packageStats.externalCodeSize,
                            externalMediaSize = packageStats.externalMediaSize,
                            externalObbSize = packageStats.externalObbSize
                        )
                    }
                }
            })

            return packageSizes
        }
    }
}