package app.simple.inure.extensions.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.services.DataLoaderService
import app.simple.inure.util.ArrayUtils

abstract class PackageUtilsViewModel(application: Application) : WrappedViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()

    private var serviceConnection: ServiceConnection? = null

    @SuppressLint("StaticFieldLeak") // This is an application context
    private var dataLoaderService: DataLoaderService? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter = IntentFilter()

    init {
        intentFilter.addAction(DataLoaderService.APPS_LOADED)
        intentFilter.addAction(DataLoaderService.UNINSTALLED_APPS_LOADED)
        intentFilter.addAction(DataLoaderService.INSTALLED_APPS_LOADED)
        intentFilter.addAction(DataLoaderService.RELOAD_APPS)

        @Suppress("UNCHECKED_CAST")
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                dataLoaderService = (service as DataLoaderService.LoaderBinder).getService()
                if (dataLoaderService!!.hasDataLoaded()) {
                    apps = dataLoaderService!!.getInstalledApps()
                    uninstalledApps = dataLoaderService!!.getUninstalledApps()

                    onAppsLoaded(apps)
                    onUninstalledAppsLoaded(uninstalledApps)
                } else {
                    dataLoaderService!!.startLoading()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                dataLoaderService = null
            }
        }

        @Suppress("UNCHECKED_CAST")
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    DataLoaderService.APPS_LOADED -> {
                        apps = dataLoaderService!!.getInstalledApps().clone() as ArrayList<PackageInfo>
                        uninstalledApps = dataLoaderService!!.getUninstalledApps().clone() as ArrayList<PackageInfo>

                        onAppsLoaded(apps)
                        onUninstalledAppsLoaded(uninstalledApps)
                    }

                    DataLoaderService.UNINSTALLED_APPS_LOADED -> {
                        uninstalledApps = dataLoaderService!!.getUninstalledApps().clone() as ArrayList<PackageInfo>
                        onUninstalledAppsLoaded(uninstalledApps)
                    }

                    DataLoaderService.INSTALLED_APPS_LOADED -> {
                        apps = dataLoaderService!!.getInstalledApps().clone() as ArrayList<PackageInfo>
                        onAppsLoaded(apps)
                    }

                    DataLoaderService.RELOAD_APPS -> {
                        Log.d("DataLoaderService", "Reloading apps")
                        dataLoaderService!!.refresh()
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(applicationContext()).registerReceiver(broadcastReceiver!!, intentFilter)
        applicationContext().bindService(
                Intent(applicationContext(), DataLoaderService::class.java), serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun getInstalledApps(): ArrayList<PackageInfo> {
        return dataLoaderService!!.getInstalledApps()
    }

    fun getUninstalledApps(): ArrayList<PackageInfo> {
        return dataLoaderService!!.getUninstalledApps()
    }

    fun getCompleteApps(): List<PackageInfo> {
        return getInstalledApps() + getUninstalledApps()
    }

    fun refreshPackageData() {
        dataLoaderService!!.refresh()
    }

    fun refreshUninstalledPackageData() {
        dataLoaderService!!.refreshUninstalled()
    }

    fun refreshInstalledPackageData() {
        dataLoaderService!!.refreshInstalled()
    }

    protected fun PackageManager.isPackageInstalled(packageName: String): Boolean {
        while (true) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageUtils.flags))
                } else {
                    getPackageInfo(packageName, PackageUtils.flags.toInt())
                }
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            } catch (e: DeadObjectException) {
                Log.e("PackageUtilsViewModel", "isPackageInstalled: DeadObjectException")
            }
        }
    }

    protected fun PackageManager.isPackageEnabled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName)!!.safeApplicationInfo.enabled
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
    protected fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String {
        while (true) {
            try {
                return context.packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                return context.getString(R.string.unknown)
            } catch (e: DeadObjectException) {
                Log.e("PackageUtilsViewModel", "getApplicationName: DeadObjectException")
            }
        }
    }

    protected fun ArrayList<PackageInfo>.loadPackageNames(): ArrayList<PackageInfo> {
        forEach {
            it.safeApplicationInfo.name = getApplicationName(applicationContext(), it.safeApplicationInfo)
        }

        return this
    }

    open fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        // Log.d("PackageUtilsViewModel", "onUninstalledAppsLoaded: ${uninstalledApps.size}")
    }

    open fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        // Log.d("PackageUtilsViewModel", "onAppsLoaded: ${apps.size}")
    }

    override fun onCleared() {
        super.onCleared()
        try {
            serviceConnection?.let {
                applicationContext().unbindService(it)
            }
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        try {
            broadcastReceiver?.let {
                LocalBroadcastManager.getInstance(applicationContext()).unregisterReceiver(it)
            }
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}
