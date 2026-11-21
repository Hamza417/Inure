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
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.services.DataLoaderRepository
import app.simple.inure.util.ArrayUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class PackageUtilsViewModel(application: Application) : WrappedViewModel(application) {

    private var apps: ArrayList<PackageInfo> = arrayListOf()
    private var uninstalledApps: ArrayList<PackageInfo> = arrayListOf()

    init {
        viewModelScope.launch(Dispatchers.Main) {
            DataLoaderRepository.apps.collect { appsList ->
                if (appsList.isNotEmpty()) {
                    apps = appsList.clone() as ArrayList<PackageInfo>
                    onAppsLoaded(apps)
                }
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            DataLoaderRepository.uninstalledApps.collect { uninstalledList ->
                if (uninstalledList.isNotEmpty()) {
                    uninstalledApps = uninstalledList.clone() as ArrayList<PackageInfo>
                    onUninstalledAppsLoaded(uninstalledApps)
                }
            }
        }
    }

    fun getInstalledApps(): ArrayList<PackageInfo> {
        return DataLoaderRepository.apps.value
    }

    fun getUninstalledApps(): ArrayList<PackageInfo> {
        return DataLoaderRepository.uninstalledApps.value
    }

    fun getCompleteApps(): List<PackageInfo> {
        return getInstalledApps() + getUninstalledApps()
    }

    fun refreshPackageData() {
        DataLoaderRepository.refresh()
    }

    fun refreshUninstalledPackageData() {
        DataLoaderRepository.refreshUninstalled()
    }

    fun refreshInstalledPackageData() {
        DataLoaderRepository.refreshInstalled()
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
            } catch (_: PackageManager.NameNotFoundException) {
                return false
            } catch (_: DeadObjectException) {
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
            } catch (_: PackageManager.NameNotFoundException) {
                return context.getString(R.string.unknown)
            } catch (_: DeadObjectException) {
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
    }
}
