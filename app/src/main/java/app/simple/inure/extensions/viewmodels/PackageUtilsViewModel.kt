package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.services.DataLoaderService
import app.simple.inure.util.ArrayUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class PackageUtilsViewModel(application: Application) : WrappedViewModel(application) {

    private var dataLoaderService: DataLoaderService? = null
    private var serviceConnection: ServiceConnection? = null

    init {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                dataLoaderService = (service as DataLoaderService.LoaderBinder).getService()
                observeServiceFlows()
                dataLoaderService?.refresh()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                dataLoaderService = null
            }
        }

        applicationContext().bindService(
                Intent(applicationContext(), DataLoaderService::class.java),
                serviceConnection!!,
                Context.BIND_AUTO_CREATE
        )
    }

    private fun observeServiceFlows() {
        viewModelScope.launch {
            dataLoaderService?.installedApps?.collectLatest { apps ->
                if (apps != null) {
                    onAppsLoaded(ArrayUtils.deepCopy(apps))
                }
            }
        }
        viewModelScope.launch {
            dataLoaderService?.uninstalledApps?.collectLatest { apps ->
                if (apps != null) {
                    onUninstalledAppsLoaded(ArrayUtils.deepCopy(apps))
                }
            }
        }
    }

    fun refreshPackageData() {
        dataLoaderService?.refresh()
    }

    // Utility and helper functions remain unchanged
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
            } catch (e: android.os.DeadObjectException) {
                Log.e("PackageUtilsViewModel", "isPackageInstalled: DeadObjectException")
            }
        }
    }

    protected fun PackageManager.isPackageEnabled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName)!!.safeApplicationInfo.enabled
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: NullPointerException) {
            false
        }
    }

    protected fun PackageManager.isPackageInstalledAndEnabled(packageName: String): Boolean {
        return isPackageInstalled(packageName) && isPackageEnabled(packageName)
    }

    protected fun PackageManager.getPackageInfo(packageName: String): PackageInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageUtils.flags))
            } else {
                try {
                    @Suppress("DEPRECATION")
                    getPackageInfo(packageName, PackageUtils.flags.toInt())
                } catch (_: RuntimeException) {
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

    protected fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String {
        while (true) {
            try {
                return context.packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (_: PackageManager.NameNotFoundException) {
                return context.getString(R.string.unknown)
            } catch (_: android.os.DeadObjectException) {
                Log.e("PackageUtilsViewModel", "getApplicationName: DeadObjectException")
            }
        }
    }

    open fun onUninstalledAppsLoaded(uninstalledApps: ArrayList<PackageInfo>) {
        // Override in subclasses
    }

    open fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        // Override in subclasses
    }

    open fun getInstalledApps(): ArrayList<PackageInfo> {
        return ArrayUtils.deepCopy(dataLoaderService?.installedApps?.value ?: ArrayList())
    }

    open fun getUninstalledApps(): ArrayList<PackageInfo> {
        return ArrayUtils.deepCopy(dataLoaderService?.uninstalledApps?.value ?: ArrayList())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            serviceConnection?.let {
                app.applicationContext.unbindService(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "PackageUtilsViewModel"
    }
}