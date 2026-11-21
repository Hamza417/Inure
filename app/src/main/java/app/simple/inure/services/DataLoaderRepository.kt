package app.simple.inure.services

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.BadParcelableException
import android.os.Build
import android.os.DeadObjectException
import android.os.DeadSystemException
import android.util.Log
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.singletons.ApplicationUtils
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.utils.DebloatUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.stream.Collectors

object DataLoaderRepository {

    private const val TAG: String = "DataLoaderRepository"

    private val _apps = MutableStateFlow<ArrayList<PackageInfo>>(arrayListOf())
    val apps: StateFlow<ArrayList<PackageInfo>> = _apps.asStateFlow()

    private val _uninstalledApps = MutableStateFlow<ArrayList<PackageInfo>>(arrayListOf())
    val uninstalledApps: StateFlow<ArrayList<PackageInfo>> = _uninstalledApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasDataLoaded = MutableStateFlow(false)
    val hasDataLoaded: StateFlow<Boolean> = _hasDataLoaded.asStateFlow()

    private val _reloadQuickApps = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val reloadQuickApps: kotlinx.coroutines.flow.SharedFlow<Unit> = _reloadQuickApps.asSharedFlow()

    private var flags = PackageManager.GET_META_DATA
    private val appContext: Context
        get() = ApplicationUtils.getApplication().applicationContext

    fun startLoading() {
        if (_isLoading.value) return
        _isLoading.value = true

        CoroutineScope(Dispatchers.IO).launch {
            val installedAppsList = if (_apps.value.isEmpty()) loadInstalledApps() else _apps.value
            val uninstalledAppsList = if (_uninstalledApps.value.isEmpty()) loadUninstalledApps() else _uninstalledApps.value

            _apps.value = installedAppsList
            _uninstalledApps.value = uninstalledAppsList

            // Init bloat list
            DebloatUtils.initBloatAppsSet()

            _hasDataLoaded.value = installedAppsList.isNotEmpty() && uninstalledAppsList.isNotEmpty()
            _isLoading.value = false
        }
    }

    fun refresh() {
        _isLoading.value = false
        _apps.value = arrayListOf()
        _uninstalledApps.value = arrayListOf()
        _hasDataLoaded.value = false
        startLoading()
    }

    fun refreshUninstalled() {
        CoroutineScope(Dispatchers.IO).launch {
            _uninstalledApps.value = loadUninstalledApps()
        }
    }

    fun refreshInstalled() {
        CoroutineScope(Dispatchers.IO).launch {
            _apps.value = loadInstalledApps()
        }
    }

    fun triggerReloadQuickApps() {
        _reloadQuickApps.tryEmit(Unit)
    }

    private fun loadInstalledApps(): ArrayList<PackageInfo> {
        val packageManager = appContext.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                return packageManager.getInstalledPackages(flags).loadPackageNames().toArrayList()
            } catch (e: DeadObjectException) {
                e.printStackTrace()
                return arrayListOf()
            } catch (e: DeadSystemException) {
                e.printStackTrace()
                return arrayListOf()
            }
        } else {
            try {
                return packageManager.getInstalledPackages(flags).loadPackageNames().toArrayList()
            } catch (e: DeadObjectException) {
                e.printStackTrace()
                return arrayListOf()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun loadUninstalledApps(): ArrayList<PackageInfo> {
        val packageManager = appContext.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.flags or PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            this.flags or PackageManager.GET_UNINSTALLED_PACKAGES
        }

        return try {
            packageManager.getInstalledPackages(flags).stream()
                .filter { packageInfo -> packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0 }
                .collect(Collectors.toList())
                .loadPackageNames()
                .toArrayList()
        } catch (e: DeadObjectException) {
            Log.w(TAG, "loadUninstalledApps: PackageManager binder died (DeadObjectException)", e)
            arrayListOf()
        } catch (e: BadParcelableException) {
            Log.w(TAG, "loadUninstalledApps: BadParcelableException", e)
            arrayListOf()
        } catch (e: Exception) {
            Log.w(TAG, "loadUninstalledApps: Exception while querying PackageManager", e)
            arrayListOf()
        }
    }

    private fun MutableList<PackageInfo>.loadPackageNames(): MutableList<PackageInfo> {
        forEach {
            it.safeApplicationInfo.name = getApplicationName(appContext, it.safeApplicationInfo)
        }
        return this
    }

    private fun getApplicationName(context: Context, applicationInfo: ApplicationInfo): String? {
        return try {
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown)
        }
    }
}
