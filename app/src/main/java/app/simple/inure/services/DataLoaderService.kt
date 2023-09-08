package app.simple.inure.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
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
    }

    private val tag: String = "DataLoaderService"
    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()
    private var downloaderThread: Thread? = null

    private var isLoading = false
    private var flags = PackageManager.GET_META_DATA

    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter = IntentFilter()

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
        Log.d(tag, "onCreate: Dataloader service created")

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
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startLoading()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy: Dataloader service destroyed")
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver!!)

        try {
            downloaderThread?.interrupt()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
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

    fun startLoading() {
        if (isLoading.invert()) {
            isLoading = true

            CoroutineScope(Dispatchers.IO).launch {
                if (apps.isEmpty()) {
                    apps = loadInstalledApps().clone()
                }

                if (uninstalledApps.isEmpty()) {
                    loadUninstalledApps()
                }

                // onAppsLoaded(apps.toArrayList())
                // onUninstalledAppsLoaded(uninstalledApps.toArrayList())

                withContext(Dispatchers.Main) {
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(APPS_LOADED))
                    isLoading = false
                }
            }
        }
    }

    fun refresh() {
        isLoading = false
        apps.clear()
        uninstalledApps.clear()
        startLoading()
    }

    fun hasDataLoaded(): Boolean {
        return apps.isNotEmpty() && uninstalledApps.isNotEmpty()
    }

    private fun loadInstalledApps(): MutableList<PackageInfo> {
        return packageManager.getInstalledPackages(flags).loadPackageNames()
    }

    private fun loadUninstalledApps() {
        if (uninstalledApps.isEmpty()) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_META_DATA or PackageManager.GET_UNINSTALLED_PACKAGES
            }

            uninstalledApps = packageManager.getInstalledPackages(flags).stream().filter { packageInfo: PackageInfo ->
                packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
            }.collect(Collectors.toList()).loadPackageNames().toArrayList()
        }
    }

    private fun MutableList<PackageInfo>.loadPackageNames(): MutableList<PackageInfo> {
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