package app.simple.inure.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.BadParcelableException
import android.os.Binder
import android.os.Build
import android.os.DeadObjectException
import android.os.DeadSystemException
import android.os.IBinder
import android.os.UserHandle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.ArrayUtils.clone
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.utils.DebloatUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.stream.Collectors

class DataLoaderService : Service() {

    companion object {
        const val UNINSTALLED_APPS_LOADED = "uninstalled_apps_loaded"
        const val INSTALLED_APPS_LOADED = "installed_apps_loaded"
        const val APPS_LOADED = "apps_loaded"
        const val RELOAD_APPS = "reload_apps"
        const val RELOAD_QUICK_APPS = "reload_quick_apps"
        const val REFRESH = "refresh"

        private const val TAG: String = "DataLoaderService"
    }

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()

    private var isLoading = false
    private var flags = PackageManager.GET_META_DATA

    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter = IntentFilter()
    private val launcherAppsService: LauncherApps by lazy {
        getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    private var launcherAppsCallback: LauncherApps.Callback? = null

    inner class LoaderBinder : Binder() {
        fun getService(): DataLoaderService {
            return this@DataLoaderService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return LoaderBinder()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Dataloader service created")

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    REFRESH -> {
                        refresh()
                    }
                }
            }
        }

        intentFilter.addAction(REFRESH)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver!!, intentFilter)

        if (launcherAppsCallback == null) {
            launcherAppsCallback = object : LauncherApps.Callback() {
                override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                    Log.d(TAG, "onPackageRemoved: $packageName")
                    refresh()
                }

                override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                    Log.d(TAG, "onPackageAdded: $packageName")
                    refresh()
                }

                override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                    Log.d(TAG, "onPackageChanged: $packageName")
                    refresh()
                }

                override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) {
                    Log.d(TAG, "onPackagesAvailable: ${packageNames?.contentToString()}")
                    refresh()
                }

                override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) {
                    Log.d(TAG, "onPackagesUnavailable: ${packageNames?.contentToString()}")
                    refresh()
                }

                fun refresh() {
                    if (DevelopmentPreferences.get(DevelopmentPreferences.REFRESH_APPS_LIST_USING_LAUNCHER_SERVICE)) {
                        this@DataLoaderService.refresh()
                    }
                }
            }

            // launcherAppsService.registerCallback(launcherAppsCallback!!)
        } else {
            Log.i(TAG, "onCreate: LauncherApps callback already initialized")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startLoading()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Dataloader service destroyed")
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver!!)
        if (launcherAppsCallback != null) {
            launcherAppsService.unregisterCallback(launcherAppsCallback)
        }
        serviceScope.cancel()
    }

    fun getInstalledApps(): ArrayList<PackageInfo> {
        if (apps.isNotNull() && apps.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            return apps.clone() as ArrayList<PackageInfo>
        } else {
            apps = loadInstalledApps() as ArrayList<PackageInfo>
            return getInstalledApps()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getUninstalledApps(): ArrayList<PackageInfo> {
        return if (uninstalledApps.isNotNull() && uninstalledApps.isNotEmpty()) {
            uninstalledApps.clone() as ArrayList<PackageInfo>
        } else {
            loadUninstalledApps()
            uninstalledApps.clone() as ArrayList<PackageInfo>
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startLoading() {
        if (isLoading.invert()) {
            isLoading = true

            serviceScope.launch {
                try {
                    if (apps.isEmpty()) {
                        apps = loadInstalledApps().clone()
                    }

                    if (uninstalledApps.isEmpty()) {
                        loadUninstalledApps()
                    }

                    // We will init bloat list here
                    // Because I couldn't think of any other place
                    DebloatUtils.initBloatAppsSet()

                    withContext(Dispatchers.Main) {
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(APPS_LOADED))
                    }
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun refresh() {
        // If a load is already running, don't spawn another one.
        if (isLoading) return
        apps.clear()
        uninstalledApps.clear()
        startLoading()
    }

    fun hasDataLoaded(): Boolean {
        return apps.isNotEmpty() && uninstalledApps.isNotEmpty()
    }

    private fun loadInstalledApps(): MutableList<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                packageManager.getInstalledPackages(flags).loadPackageNames()
            } catch (e: DeadObjectException) {
                e.printStackTrace()
                mutableListOf()
            } catch (e: DeadSystemException) {
                e.printStackTrace()
                mutableListOf()
            } catch (e: RuntimeException) {
                // some OEMs wrap binder/system-death failures in RuntimeException.
                e.printStackTrace()
                mutableListOf()
            }
        } else {
            try {
                packageManager.getInstalledPackages(flags).loadPackageNames()
            } catch (e: DeadObjectException) {
                e.printStackTrace()
                mutableListOf()
            } catch (e: RuntimeException) {
                e.printStackTrace()
                mutableListOf()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun loadUninstalledApps() {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.flags or PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            this.flags or PackageManager.GET_UNINSTALLED_PACKAGES
        }

        if (uninstalledApps.isNotEmpty()) return

        try {
            uninstalledApps = packageManager.getInstalledPackages(flags).stream()
                .filter { packageInfo -> packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0 }
                .collect(Collectors.toList())
                .loadPackageNames()
                .toArrayList()
        } catch (e: DeadObjectException) {
            uninstalledApps = arrayListOf()
            Log.w(TAG, "loadUninstalledApps: PackageManager binder died (DeadObjectException)", e)
        } catch (e: BadParcelableException) {
            uninstalledApps = arrayListOf()
            Log.w(TAG, "loadUninstalledApps: BadParcelableException", e)
        } catch (e: RuntimeException) {
            // Includes system-death cases on older APIs / OEMs.
            uninstalledApps = arrayListOf()
            Log.w(TAG, "loadUninstalledApps: RuntimeException while querying PackageManager", e)
        }
    }

    private fun MutableList<PackageInfo>.loadPackageNames(): MutableList<PackageInfo> {
        forEach {
            it.safeApplicationInfo.name = getApplicationName(application.applicationContext, it.safeApplicationInfo)
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
    private fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String? {
        return try {
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }

    private fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        Log.d("DataLoaderService", "onUninstalledAppsLoaded: ${uninstalledApps.size}")
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(UNINSTALLED_APPS_LOADED))
    }

    private fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        Log.d("DataLoaderService", "onAppsLoaded: ${apps.size}")
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(INSTALLED_APPS_LOADED))
    }

    @Suppress("UNCHECKED_CAST")
    fun refreshUninstalled() {
        uninstalledApps.clear()
        loadUninstalledApps()
        onUninstalledAppsLoaded(uninstalledApps.clone() as ArrayList<PackageInfo>)
    }

    @Suppress("UNCHECKED_CAST")
    fun refreshInstalled() {
        apps.clear()
        apps = loadInstalledApps() as ArrayList<PackageInfo>
        onAppsLoaded(apps.clone() as ArrayList<PackageInfo>)
    }
}
