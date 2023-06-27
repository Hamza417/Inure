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
import kotlinx.coroutines.withContext
import java.util.stream.Collectors

class DataLoaderService : Service() {

    companion object {
        const val UNINSTALLED_APPS_LOADED = "uninstalled_apps_loaded"
        const val INSTALLED_APPS_LOADED = "installed_apps_loaded"
        const val APPS_LOADED = "apps_loaded"
    }

    private val TAG: String = "DataLoaderService"
    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()

    private var isLoading = false

    inner class LoaderBinder : Binder() {
        fun getService(): DataLoaderService {
            return this@DataLoaderService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return LoaderBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Default).launch {
            startLoading()

            withContext(Dispatchers.Main) {
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(APPS_LOADED))
            }
        }

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

                onAppsLoaded(apps.toArrayList())
                onUninstalledAppsLoaded(uninstalledApps.toArrayList())

                withContext(Dispatchers.Main) {
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(APPS_LOADED))
                    Log.d(TAG, "startLoading: apps loaded")
                    isLoading = false
                }
            }
        }
    }

    fun refresh() {
        isLoading = false
        startLoading()
    }

    fun hasDataLoaded(): Boolean {
        return apps.isNotEmpty() && uninstalledApps.isNotEmpty()
    }

    private fun loadInstalledApps(): MutableList<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong())).loadPackageNames()
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA).loadPackageNames()
        }
    }

    private fun loadUninstalledApps() {
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
            }.stream().filter { packageInfo: PackageInfo ->
                packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
            }.collect(Collectors.toList()).toArrayList()
        }

        @Suppress("UNCHECKED_CAST")
        onUninstalledAppsLoaded(apps.clone() as ArrayList<PackageInfo>)
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

    fun refreshUninstalled() {
        uninstalledApps.clear()
        loadUninstalledApps()
    }

    fun refreshInstalled() {
        apps.clear()
        apps = loadInstalledApps() as ArrayList<PackageInfo>
    }
}