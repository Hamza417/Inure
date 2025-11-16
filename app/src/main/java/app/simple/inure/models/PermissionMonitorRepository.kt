package app.simple.inure.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Singleton repository for live permission monitoring data
 * Mediates between PermissionMonitorService and UI components
 * Maintains a log of all permission usage, bubbling recent usage to the top
 */
object PermissionMonitorRepository {

    private val TAG = "PermissionMonitorRepo"

    // Historical log of all permission usage - never cleared unless explicitly requested
    private val permissionUsageMap = mutableMapOf<String, PermissionUsage>()

    private val _activePermissions = MutableLiveData<ArrayList<PermissionUsage>>(arrayListOf())
    val activePermissions: LiveData<ArrayList<PermissionUsage>> = _activePermissions

    private val _isMonitoring = MutableLiveData<Boolean>(false)
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    /**
     * Add or update a permission usage entry
     * This builds a log over time - entries bubble to top when used
     */
    fun addOrUpdatePermission(usage: PermissionUsage) {
        synchronized(permissionUsageMap) {
            val key = "${usage.packageInfo.packageName}:${usage.permissionId}"
            permissionUsageMap[key] = usage

            // Sort by most recent access time (newest first)
            val sortedList = ArrayList(permissionUsageMap.values.sortedByDescending { it.lastAccessTime })

            Log.d(TAG, "Updated permission log. Total entries: ${sortedList.size}")
            _activePermissions.postValue(sortedList)
        }
    }

    /**
     * Update active status for a permission without changing its position in history
     */
    fun updatePermissionStatus(packageName: String, permissionId: String, isActive: Boolean) {
        synchronized(permissionUsageMap) {
            val key = "$packageName:$permissionId"
            permissionUsageMap[key]?.let { existing ->
                val updated = existing.copy(
                    isActive = isActive,
                    lastAccessTime = if (isActive) System.currentTimeMillis() else existing.lastAccessTime
                )
                permissionUsageMap[key] = updated

                // Sort by most recent access time (newest first)
                val sortedList = ArrayList(permissionUsageMap.values.sortedByDescending { it.lastAccessTime })

                Log.d(TAG, "Updated status for $packageName:$permissionId to active=$isActive")
                _activePermissions.postValue(sortedList)
            }
        }
    }

    /**
     * Called by PermissionMonitorService to update active permissions
     * This method now adds to the log instead of replacing
     */
    fun updateActivePermissions(permissions: ArrayList<PermissionUsage>) {
        synchronized(permissionUsageMap) {
            // Add or update each permission in the log
            for (usage in permissions) {
                val key = "${usage.packageInfo.packageName}:${usage.permissionId}"
                permissionUsageMap[key] = usage
            }

            // Sort by most recent access time (newest first)
            val sortedList = ArrayList(permissionUsageMap.values.sortedByDescending { it.lastAccessTime })

            Log.d(TAG, "Batch update. Total entries in log: ${sortedList.size}")
            _activePermissions.postValue(sortedList)
        }
    }

    /**
     * Called by PermissionMonitorService to update monitoring status
     */
    fun setMonitoring(isMonitoring: Boolean) {
        _isMonitoring.postValue(isMonitoring)
    }

    /**
     * Clear all historical data
     */
    fun clear() {
        synchronized(permissionUsageMap) {
            permissionUsageMap.clear()
            _activePermissions.postValue(arrayListOf())
            Log.d(TAG, "Cleared all permission usage history")
        }
    }
}
