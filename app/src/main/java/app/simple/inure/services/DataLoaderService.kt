package app.simple.inure.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.UserHandle
import android.util.Log
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.utils.DebloatUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataLoaderService : Service() {

    companion object {
        private const val TAG: String = "DataLoaderService"

        private const val INSTALLED_FLAGS = PackageManager.GET_META_DATA

        @Suppress("DEPRECATION")
        private val UNINSTALLED_FLAGS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            INSTALLED_FLAGS or PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            INSTALLED_FLAGS or PackageManager.GET_UNINSTALLED_PACKAGES
        }

        const val REFRESH = "app.simple.inure.services.DataLoaderService.REFRESH"
        const val RELOAD_QUICK_APPS = "app.simple.inure.services.DataLoaderService.RELOAD_QUICK_APPS"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _installedApps = MutableStateFlow<ArrayList<PackageInfo>?>(null)
    val installedApps: StateFlow<ArrayList<PackageInfo>?> = _installedApps

    private val _uninstalledApps = MutableStateFlow<ArrayList<PackageInfo>?>(null)
    val uninstalledApps: StateFlow<ArrayList<PackageInfo>?> = _uninstalledApps

    private var isLoading = false

    private val launcherAppsService: LauncherApps by lazy {
        getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    private var launcherAppsCallback: LauncherApps.Callback? = null

    inner class LoaderBinder : Binder() {
        fun getService(): DataLoaderService = this@DataLoaderService
    }

    override fun onBind(intent: Intent): IBinder = LoaderBinder()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Dataloader service created")

        if (launcherAppsCallback == null) {
            launcherAppsCallback = object : LauncherApps.Callback() {
                override fun onPackageRemoved(packageName: String?, user: UserHandle?) = refresh()
                override fun onPackageAdded(packageName: String?, user: UserHandle?) = refresh()
                override fun onPackageChanged(packageName: String?, user: UserHandle?) = refresh()
                override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
                override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
                fun refresh() {
                    if (DevelopmentPreferences.get(DevelopmentPreferences.REFRESH_APPS_LIST_USING_LAUNCHER_SERVICE)) {
                        this@DataLoaderService.refresh()
                    }
                }
            }
            // launcherAppsService.registerCallback(launcherAppsCallback!!)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        refresh()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Dataloader service destroyed")
        serviceScope.cancel()
        if (launcherAppsCallback != null) {
            launcherAppsService.unregisterCallback(launcherAppsCallback)
        }
    }

    fun refresh() {
        if (isLoading.invert()) {
            Log.i(TAG, "refresh: Loading installed and uninstalled apps")

            isLoading = true
            serviceScope.launch {
                val installed = loadInstalledApps()
                val uninstalled = loadUninstalledApps()

                withContext(Dispatchers.Main) {
                    _installedApps.value = installed
                    _uninstalledApps.value = uninstalled
                    isLoading = false
                }
                Log.i(TAG, "refresh: Loaded ${installed.size} installed and ${uninstalled.size} uninstalled apps")

                DebloatUtils.initBloatAppsSet()
            }
        } else {
            Log.w(TAG, "refresh: Already loading apps, skipping refresh")
        }
    }

    private suspend fun loadInstalledApps(maxRetries: Int = 3): ArrayList<PackageInfo> {
        val result = ArrayList<PackageInfo>()
        repeat(maxRetries) { attempt ->
            try {
                val packageNames = packageManager.getInstalledPackages(0).map { it.packageName }
                packageNames.chunked(100).forEach { batch ->
                    batch.forEach { pkg ->
                        try {
                            val info = packageManager.getPackageInfo(pkg, INSTALLED_FLAGS)
                            result.add(info)
                        } catch (_: Exception) {
                        }
                    }
                }
                result.forEach {
                    it.safeApplicationInfo.name = getApplicationName(applicationContext, it.safeApplicationInfo)
                }
                return result
            } catch (e: Exception) {
                result.clear()
                e.printStackTrace()
                if (attempt < maxRetries - 1) {
                    delay(500L)
                }
            }
        }
        return result
    }

    private suspend fun loadUninstalledApps(maxRetries: Int = 3): ArrayList<PackageInfo> {
        val result = ArrayList<PackageInfo>()

        repeat(maxRetries) { attempt ->
            try {
                val packageNames = packageManager.getInstalledPackages(0).map { it.packageName }
                packageNames.chunked(100).forEach { batch ->
                    batch.forEach { pkg ->
                        try {
                            val info = packageManager.getPackageInfo(pkg, UNINSTALLED_FLAGS)
                            if (info.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                                result.add(info)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
                result.forEach {
                    it.safeApplicationInfo.name = getApplicationName(applicationContext, it.safeApplicationInfo)
                }
                return result
            } catch (e: Exception) {
                result.clear()
                e.printStackTrace()
                if (attempt < maxRetries - 1) {
                    delay(500L)
                }
            }
        }
        return result
    }

    private fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String? {
        return try {
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }
}