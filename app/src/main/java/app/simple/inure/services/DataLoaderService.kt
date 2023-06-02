package app.simple.inure.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.util.ArrayUtils.clone
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataLoaderService : Service() {

    companion object {
        val UNINSTALLED_APPS_LOADED = "uninstalled_apps_loaded"
        val INSTALLED_APPS_LOADED = "installed_apps_loaded"
    }

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()

    private var areAppsLoadingStarted = false

    inner class LocalBinder : Binder() {
        fun getService(): DataLoaderService {
            return this@DataLoaderService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        loadPackageData()
        return super.onStartCommand(intent, flags, startId)
    }

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

    @Suppress("UNCHECKED_CAST")
    fun getUninstalledApps(): ArrayList<PackageInfo> {
        if (uninstalledApps.isNotNull() && uninstalledApps.isNotEmpty()) {
            return uninstalledApps.clone() as ArrayList<PackageInfo>
        } else {
            loadUninstalledApps()
            return uninstalledApps.clone() as ArrayList<PackageInfo>
        }
    }

    protected fun loadPackageData() {
        if (areAppsLoadingStarted.invert()) {
            areAppsLoadingStarted = true

            CoroutineScope(Dispatchers.IO).launch {
                if (apps.isEmpty()) {
                    apps = loadInstalledApps().clone()
                }

                if (uninstalledApps.isEmpty()) {
                    loadUninstalledApps()
                }

                onAppsLoaded(apps.toArrayList())
                onUninstalledAppsLoaded(uninstalledApps.toArrayList())
            }
        }
    }

    fun refreshPackageData() {
        CoroutineScope(Dispatchers.IO).launch {
            apps = loadInstalledApps().clone()
            onAppsLoaded(apps.toArrayList())
        }
    }

    private fun loadInstalledApps(): MutableList<PackageInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong())).loadPackageNames()
                } catch (e: DeadObjectException) {
                    Log.e("PackageUtilsViewModel", "loadInstalledApps: DeadObjectException")
                    loadInstalledApps()
                }
            } else {
                try {
                    @Suppress("DEPRECATION")
                    packageManager.getInstalledPackages(PackageManager.GET_META_DATA).loadPackageNames()
                } catch (e: DeadObjectException) {
                    Log.e("PackageUtilsViewModel", "loadInstalledApps: DeadObjectException")
                    loadInstalledApps()
                }
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
                                                                    or PackageManager.MATCH_UNINSTALLED_PACKAGES).toLong())).loadPackageNames()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        @Suppress("DEPRECATION")
                        packageManager.getInstalledPackages(PackageManager.GET_META_DATA
                                                                    or PackageManager.MATCH_UNINSTALLED_PACKAGES).loadPackageNames()
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getInstalledPackages(PackageManager.GET_META_DATA
                                                                    or PackageManager.GET_UNINSTALLED_PACKAGES).loadPackageNames()
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

    fun MutableList<PackageInfo>.loadPackageNames(): MutableList<PackageInfo> {
        forEach {
            it.applicationInfo.name = getApplicationName(application.applicationContext, it.applicationInfo)
        }

        return this
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
        } catch (e: DeadObjectException) {
            Log.e("PackageUtilsViewModel", "getApplicationName: DeadObjectException")
            getApplicationName(context, applicationInfo)
        }
    }

    private fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        Log.d("PackageUtilsViewModel", "onUninstalledAppsLoaded: ${uninstalledApps.size}")
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(UNINSTALLED_APPS_LOADED))
    }

    private fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        Log.d("PackageUtilsViewModel", "onAppsLoaded: ${apps.size}")
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(INSTALLED_APPS_LOADED))
    }
}