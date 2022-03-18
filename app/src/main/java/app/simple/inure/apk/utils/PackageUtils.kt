package app.simple.inure.apk.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.*
import android.net.Uri
import android.os.Build
import android.os.RemoteException
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import app.simple.inure.R
import app.simple.inure.models.PackageSizes
import app.simple.inure.util.DateUtils
import java.lang.reflect.Method


object PackageUtils {

    private const val UNINSTALL_REQUEST_CODE = 6452

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
     * Fetches the app's name from the package id of the same application
     * @param context of the given environment
     * @param packageName is [ApplicationInfo.packageName] app's package name
     * @return app's name as [String]
     */
    fun getApplicationName(context: Context, packageName: String): String? {
        return try {
            val p0 = context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            return context.packageManager.getApplicationLabel(p0).toString()
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
    fun getApplicationVersion(context: Context, applicationInfo: PackageInfo): String {
        return try {
            context.packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_META_DATA).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        } catch (e: NullPointerException) {
            context.getString(R.string.unknown)
        }
    }

    /**
     * Fetches the app's version code from the package id of the application
     * @param context of the given environment
     * @param packageInfo is [ApplicationInfo] object containing app's
     *        information
     * @return app's version code as [String]
     */
    fun getApplicationVersionCode(context: Context, packageInfo: PackageInfo): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(packageInfo.packageName, 0).longVersionCode.toString()
            } else {
                @Suppress("deprecation")
                context.packageManager.getPackageInfo(packageInfo.packageName, 0).versionCode.toString()
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
    fun PackageInfo.getApplicationInstallTime(context: Context, pattern: String): String {
        return try {
            DateUtils.formatDate(context.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime, pattern)
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
    fun PackageInfo.getApplicationLastUpdateTime(context: Context, pattern: String): String {
        return try {
            DateUtils.formatDate(context.packageManager.getPackageInfo(this.packageName, 0).lastUpdateTime, pattern)
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }

    fun checkIfAppIsLaunchable(context: Context, packageName: String): Boolean {
        return context.packageManager
            .getLaunchIntentForPackage(packageName) != null
    }

    fun PackageInfo.launchThisPackage(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(this.packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    fun launchThisPackage(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    /**
     * this function kills an app using app's package id as
     * identifier, system apps will not be killed.
     */
    fun PackageInfo.killThisApp(activity: Activity) {
        val mActivityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (this.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
            Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.warning_kill_system_app), Toast.LENGTH_SHORT)
                .show()
        } else {
            mActivityManager.killBackgroundProcesses(this.packageName)
            Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.alert_app_killed), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    fun PackageInfo.isPackageInstalled(packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
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
    fun PackageInfo.uninstallThisPackage(appUninstallObserver: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        intent.data = Uri.parse("package:${this.packageName}")
        appUninstallObserver.launch(intent)
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun isPackageEnabled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            val p0 = packageManager.getPackageInfo(packageName, 0)
            p0.applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isPackageInstalledAndEnabled(packageName: String, packageManager: PackageManager): Boolean {
        return isPackageInstalled(packageName, packageManager) && isPackageEnabled(packageName, packageManager)
    }

    /**
     * Fetches the directory size of this installed application
     * @return [Long] and should be formatted manually
     */
    fun PackageInfo.getPackageSize(context: Context): PackageSizes {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            return try {
                val storageStats = storageStatsManager.queryStatsForUid(this.applicationInfo.storageUuid, this.applicationInfo.uid)
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