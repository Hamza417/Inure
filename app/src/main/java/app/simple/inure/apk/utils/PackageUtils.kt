package app.simple.inure.apk.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.*
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.os.RemoteException
import androidx.activity.result.ActivityResultLauncher
import app.simple.inure.R
import app.simple.inure.models.PackageSizes
import app.simple.inure.util.ArrayUtils
import app.simple.inure.util.DateUtils
import java.io.File
import java.lang.reflect.Method
import java.util.*

@Suppress("KotlinRedundantDiagnosticSuppress")
object PackageUtils {

    private const val UNINSTALL_REQUEST_CODE = 6452

    val flags: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        (PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_CONFIGURATIONS or
                PackageManager.MATCH_DISABLED_COMPONENTS or
                PackageManager.MATCH_UNINSTALLED_PACKAGES).toLong()
    } else {
        @Suppress("DEPRECATION")
        (PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_CONFIGURATIONS or
                PackageManager.GET_DISABLED_COMPONENTS or
                PackageManager.GET_UNINSTALLED_PACKAGES).toLong()
    }

    private const val PRIVATE_FLAG_HIDDEN = 1 shl 0

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
        } catch (e: NameNotFoundException) {
            try {
                context.packageManager.getPackageArchiveInfo(applicationInfo.sourceDir)?.let {
                    context.packageManager.getApplicationLabel(it.applicationInfo).toString()
                }
            } catch (e: NameNotFoundException) {
                context.getString(R.string.unknown)
            }
        }
    }

    fun PackageManager.getPackageInfo(packageName: String): PackageInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags))
            } else {
                try {
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageName, flags.toInt())
                } catch (e: RuntimeException) {
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageName, PackageManager.GET_META_DATA)
                }
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun PackageManager.getPackageInfo(packageName: String, flags: Int): PackageInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                getPackageInfo(packageName, flags)
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    fun PackageManager.getPackageArchiveInfo(path: String): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(flags))
            } else {
                @Suppress("DEPRECATION")
                getPackageArchiveInfo(path, flags.toInt())
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun PackageManager.getPackageArchiveInfo(path: String, flags: Int): PackageInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                getPackageArchiveInfo(path, flags)
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    fun PackageManager.getPackageArchiveInfo(file: File): PackageInfo? {
        return getPackageArchiveInfo(file.absolutePath)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun PackageManager.getApplicationInfo(packageName: String): ApplicationInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(flags))
            } else {
                @Suppress("DEPRECATION")
                getApplicationInfo(packageName, flags.toInt())
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Fetches the app's name from the package id of the same application
     * @param context of the given environment
     * @param packageName is [ApplicationInfo.packageName] app's package name
     * @return app's name as [String]
     */
    fun getApplicationName(context: Context, packageName: String): String {
        return kotlin.runCatching {
            val p0 = context.packageManager.getApplicationInfo(packageName)
            return context.packageManager.getApplicationLabel(p0!!).toString()
        }.getOrElse {
            context.getString(R.string.unknown)
        }
    }

    /**
     * Fetches the app's version name from the package id of the application
     * @param context of the given environment
     * @param packageInfo is [ApplicationInfo] object containing app's
     *        information
     * @return app's version name as [String]
     */
    fun getApplicationVersion(context: Context, packageInfo: PackageInfo): String {
        return try {
            context.packageManager.getPackageInfo(packageInfo.packageName)!!.versionName
        } catch (e: NameNotFoundException) {
            try {
                context.packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!.versionName
            } catch (e: NameNotFoundException) {
                context.getString(R.string.unknown)
            } catch (e: NullPointerException) {
                context.getString(R.string.unknown)
            }
        } catch (e: NullPointerException) {
            try {
                context.packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!.versionName
            } catch (e: NameNotFoundException) {
                context.getString(R.string.unknown)
            } catch (e: NullPointerException) {
                context.getString(R.string.unknown)
            }
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
                context.packageManager.getPackageInfo(packageInfo.packageName)!!.longVersionCode.toString()
            } else {
                @Suppress("deprecation")
                context.packageManager.getPackageInfo(packageInfo.packageName)!!.versionCode.toString()
            }
        } catch (e: NameNotFoundException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!.longVersionCode.toString()
                } else {
                    @Suppress("deprecation")
                    context.packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!.versionCode.toString()
                }
            } catch (e: NameNotFoundException) {
                context.getString(R.string.unknown)
            } catch (e: NullPointerException) {
                context.getString(R.string.unknown)
            }
        } catch (e: NullPointerException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!.longVersionCode.toString()
                } else {
                    @Suppress("deprecation")
                    context.packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!.versionCode.toString()
                }
            } catch (e: NameNotFoundException) {
                context.getString(R.string.unknown)
            } catch (e: NullPointerException) {
                context.getString(R.string.unknown)
            }
        }
    }

    /**
     * Check if app is a system app
     */
    fun PackageInfo.isSystemApp(): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun PackageInfo.isSplitApk(): Boolean {
        return applicationInfo.splitSourceDirs.isNullOrEmpty().not()
    }

    /**
     * Check if app is a system app
     */
    fun ApplicationInfo.isSystemApp(): Boolean {
        return flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    /**
     * Check if app is a user app
     */
    fun PackageInfo.isUserApp(): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
    }

    /**
     * Check if app is a user app
     */
    @Suppress("unused")
    fun ApplicationInfo.isUserApp(): Boolean {
        return flags and ApplicationInfo.FLAG_SYSTEM == 0
    }

    fun ApplicationInfo.isXposedModule(): Boolean {
        return kotlin.runCatching {
            metaData.containsKey("xposedmodule")
        }.getOrElse {
            false
        }
    }

    fun ApplicationInfo.getXposedDescription(): String {
        return kotlin.runCatching {
            metaData.getString("xposeddescription") ?: ""
        }.getOrElse {
            ""
        }
    }

    /**
     * Check if an update is installed for a system app
     */
    fun PackageInfo.isUpdateInstalled(): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
    }

    /**
     * Check if the app is installed
     */
    fun PackageInfo.isInstalled(): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED != 0
    }

    /**
     * Fetches the app's install date from the package id of the application
     * @param context of the given environment
     * @return app's install date as [String]
     */
    fun PackageInfo.getApplicationInstallTime(context: Context, pattern: String): String {
        return try {
            DateUtils.formatDate(context.packageManager.getPackageInfo(this.packageName)!!.firstInstallTime, pattern)
        } catch (e: NameNotFoundException) {
            context.getString(R.string.unknown)
        } catch (e: NullPointerException) {
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
            DateUtils.formatDate(context.packageManager.getPackageInfo(this.packageName)!!.lastUpdateTime, pattern)
        } catch (e: NameNotFoundException) {
            context.getString(R.string.unknown)
        } catch (e: NullPointerException) {
            context.getString(R.string.unknown)
        }
    }

    fun checkIfAppIsLaunchable(context: Context, packageName: String): Boolean {
        return context.packageManager
            .getLaunchIntentForPackage(packageName) != null
    }

    @Throws(NameNotFoundException::class, NullPointerException::class)
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
    @Suppress("unused")
    fun PackageInfo.killThisApp(activity: Activity) {
        val mActivityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (this.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
            // Killed
        } else {
            mActivityManager.killBackgroundProcesses(this.packageName)
        }
    }

    fun PackageManager.isPackageInstalled(packageName: String): Boolean {
        var tryUpTo = 3

        while (tryUpTo != 0) {
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageName, 0)
                }
                return true
            }.getOrElse {
                if (it is NameNotFoundException) {
                    return false
                } else {
                    tryUpTo--
                }
            }
        }

        return false
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

    private fun PackageManager.isPackageEnabled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName)!!.applicationInfo.enabled
        } catch (e: NameNotFoundException) {
            false
        } catch (e: NullPointerException) {
            false
        }
    }

    fun PackageManager.isPackageInstalledAndEnabled(packageName: String): Boolean {
        return isPackageInstalled(packageName) && isPackageEnabled(packageName)
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

    @Suppress("unused")
    fun convertS(digest: ByteArray): String {
        var s = ""
        for (b in digest) {
            s += String.format("%02X", b).lowercase(Locale.getDefault())
        }
        return s
    }

    /**
     * Can cause reference issues if list is modified.
     * All objects returned in this list are immutable and share the same reference.
     * @warning do not modify the returned [PackageInfo] object
     * @return [ArrayList] of [PackageInfo] objects
     */
    @Suppress("unused")
    fun PackageManager.getInstalledPackages(flags: Long = PackageUtils.flags): ArrayList<PackageInfo> {
        val packageInfoList = ArrayList<PackageInfo>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageInfoList.addAll(getInstalledPackages(PackageManager.PackageInfoFlags.of(flags)))
        } else {
            packageInfoList.addAll(getInstalledPackages(flags.toInt()))
        }
        return ArrayUtils.deepCopy(packageInfoList)
    }

    fun PackageManager.isAppHidden(packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getApplicationInfo(packageName)?.flags!! and PRIVATE_FLAG_HIDDEN != 0
        } else {
            false
        }
    }

    fun getIntentFilter(s: String): Intent {
        return Intent(s)
    }
}