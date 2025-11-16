package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.PermissionMonitorRepository
import app.simple.inure.models.PermissionUsage
import app.simple.inure.services.PermissionMonitorService

/**
 * ViewModel for real-time permission monitoring
 * Observes live updates from PermissionMonitorService via LiveData repository
 */
class UsageMonitorViewModel(application: Application) : WrappedViewModel(application) {

    private val permissionUsageData: MediatorLiveData<ArrayList<PermissionUsage>> = MediatorLiveData()
    private val isServiceRunning: LiveData<Boolean> = PermissionMonitorRepository.isMonitoring

    private var currentKeyword: String = ""

    init {
        Log.d(TAG, "=== LOGGING VERSION 2025-11-17-B INITIALIZED ===")
        // Observe repository LiveData and apply filtering
        permissionUsageData.addSource(PermissionMonitorRepository.activePermissions) { activeOps ->
            Log.d(TAG, "Received ${activeOps.size} active operations from repository")
            val filtered = filterPermissionUsage(activeOps, currentKeyword)
            permissionUsageData.value = filtered
            Log.d(TAG, "Filtered to ${filtered.size} operations")
        }
    }

    fun getPermissionUsageData(): LiveData<ArrayList<PermissionUsage>> {
        return permissionUsageData
    }

    fun getIsServiceRunning(): LiveData<Boolean> {
        return isServiceRunning
    }

    fun loadPermissionUsageData(keyword: String = "") {
        currentKeyword = keyword
        // Reapply filter with new keyword
        PermissionMonitorRepository.activePermissions.value?.let { activeOps ->
            val filtered = filterPermissionUsage(activeOps, currentKeyword)
            permissionUsageData.value = filtered
        }
    }

    fun startMonitoring() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            postWarning("Live monitoring requires Android 10 (API 29) or higher")
            return
        }

        val intent = Intent(getApplication(), PermissionMonitorService::class.java).apply {
            action = PermissionMonitorService.ACTION_START_MONITORING
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplication().startForegroundService(intent)
            } else {
                getApplication().startService(intent)
            }
            Log.d(TAG, "Monitoring service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start monitoring service", e)
            postWarning("Failed to start monitoring service: ${e.message}")
        }
    }

    fun stopMonitoring() {
        val intent = Intent(getApplication(), PermissionMonitorService::class.java).apply {
            action = PermissionMonitorService.ACTION_STOP_MONITORING
        }

        try {
            getApplication().startService(intent)
            Log.d(TAG, "Monitoring service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop monitoring service", e)
        }
    }

    private fun filterPermissionUsage(
        usageList: ArrayList<PermissionUsage>,
        keyword: String
    ): ArrayList<PermissionUsage> {
        if (keyword.isEmpty()) return usageList

        val filtered = arrayListOf<PermissionUsage>()
        val lowercaseKeyword = keyword.lowercase()

        for (usage in usageList) {
            val appName = usage.packageInfo.applicationInfo?.loadLabel(
                getApplication().packageManager
            )?.toString() ?: usage.packageInfo.packageName
            val packageName = usage.packageInfo.packageName
            val permission = usage.permission.lowercase()

            if (appName.lowercase().contains(lowercaseKeyword) ||
                packageName.lowercase().contains(lowercaseKeyword) ||
                permission.contains(lowercaseKeyword)
            ) {
                filtered.add(usage)
            }
        }

        return filtered
    }

    companion object {
        private const val TAG = "UsageMonitorViewModel"
    }
}
