package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.DeadObjectException
import android.util.Log
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.util.ArrayUtils
import app.simple.inure.util.ArrayUtils.clone
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class PackageUtilsViewModel(application: Application) : WrappedViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()

    private var areAppsLoadingStarted = false

    fun getInstalledApps(): ArrayList<PackageInfo> {
        if (apps.isNotNull() && apps.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            return apps.clone() as ArrayList<PackageInfo>
        } else {
            Log.d("PackageUtilsViewModel", "getInstalledApps: apps is null or empty, reloading")
            apps = loadInstalledApps() as ArrayList<PackageInfo>
            return getInstalledApps()
        }
    }

    fun getUninstalledApps(): ArrayList<PackageInfo> {
        @Suppress("UNCHECKED_CAST")
        return uninstalledApps.clone() as ArrayList<PackageInfo>
    }

    protected fun loadPackageData() {
        if (areAppsLoadingStarted.invert()) {
            areAppsLoadingStarted = true

            viewModelScope.launch(Dispatchers.IO) {
                if (apps.isEmpty()) {
                    apps = loadInstalledApps().clone()
                }
                onAppsLoaded(apps.toArrayList())
            }
        }
    }

    private fun loadInstalledApps(): MutableList<PackageInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }
        } catch (e: DeadObjectException) {
            Log.e("PackageUtilsViewModel", "loadInstalledApps: DeadObjectException")
            loadInstalledApps()
        }
    }

    protected fun loadUninstalledApps() {
        try {
            if (uninstalledApps.isEmpty()) {
                uninstalledApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags
                                                            .of((PackageManager.GET_META_DATA
                                                                    or PackageManager.MATCH_UNINSTALLED_PACKAGES).toLong()))
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        @Suppress("DEPRECATION")
                        packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES)
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.GET_UNINSTALLED_PACKAGES)
                    }
                }.toArrayList()
            }

            @Suppress("UNCHECKED_CAST")
            onUninstalledAppsLoaded(apps.clone() as ArrayList<PackageInfo>)
        } catch (e: DeadObjectException) {
            Log.e("PackageUtilsViewModel", "loadUninstalledApps: DeadObjectException")
            loadUninstalledApps()
        }
    }

    protected fun PackageManager.isPackageInstalled(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageUtils.flags))
            } else {
                @Suppress("DEPRECATION")
                getPackageInfo(packageName, PackageUtils.flags.toInt())
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    protected fun PackageManager.isPackageEnabled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName)!!.applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: NullPointerException) {
            false
        }
    }

    protected fun PackageManager.isPackageInstalledAndEnabled(packageName: String): Boolean {
        return isPackageInstalled(packageName) && isPackageEnabled(packageName)
    }

    protected fun PackageManager.getInstalledPackages(flags: Long = PackageUtils.flags): ArrayList<PackageInfo> {
        val packageInfoList = ArrayList<PackageInfo>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageInfoList.addAll(getInstalledPackages(PackageManager.PackageInfoFlags.of(flags)))
        } else {
            @Suppress("DEPRECATION")
            packageInfoList.addAll(getInstalledPackages(flags.toInt()))
        }
        return ArrayUtils.deepCopy(packageInfoList)
    }

    protected fun PackageManager.getPackageInfo(packageName: String): PackageInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageUtils.flags))
            } else {
                try {
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageName, PackageUtils.flags.toInt())
                } catch (e: RuntimeException) {
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageName, PackageManager.GET_META_DATA)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun PackageManager.getApplicationInfo(packageName: String): ApplicationInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageUtils.flags))
            } else {
                @Suppress("DEPRECATION")
                getApplicationInfo(packageName, PackageUtils.flags.toInt())
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Fetches the app's name from the package id of the same application
     * @param context of the given environment
     * @param applicationInfo is [ApplicationInfo] object containing app's
     *        information
     * @return app's name as [String]
     */
    protected fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String? {
        return try {
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }

    open fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        Log.d("PackageUtilsViewModel", "onUninstalledAppsLoaded: ${uninstalledApps.size}")
    }

    open fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        Log.d("PackageUtilsViewModel", "onAppsLoaded: ${apps.size}")
    }
}