package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.PermissionUsage
import app.simple.inure.services.PermissionMonitorService

/**
 * ViewModel for real-time permission monitoring
 * Receives live updates from PermissionMonitorService via broadcasts
 */
class UsageMonitorViewModel(application: Application) : WrappedViewModel(application) {

    private val permissionUsageData: MutableLiveData<ArrayList<PermissionUsage>> = MutableLiveData(arrayListOf())
    private val isServiceRunning: MutableLiveData<Boolean> = MutableLiveData(false)

    private var currentKeyword: String = ""

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PermissionMonitorService.BROADCAST_ACTIVE_OPS -> {
                    handleActiveOpsUpdate(intent)
                }
                PermissionMonitorService.BROADCAST_OP_CHANGED -> {
                    handleOpChanged(intent)
                }
            }
        }
    }

    init {
        registerBroadcastReceiver()
    }

    fun getPermissionUsageData(): LiveData<ArrayList<PermissionUsage>> {
        return permissionUsageData
    }

    fun getIsServiceRunning(): LiveData<Boolean> {
        return isServiceRunning
    }

    fun loadPermissionUsageData(keyword: String = "") {
        currentKeyword = keyword
        // Request update from service
        requestServiceUpdate()
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
                getApplication<Application>().startForegroundService(intent)
            } else {
                getApplication<Application>().startService(intent)
            }
            isServiceRunning.postValue(true)
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
            getApplication<Application>().startService(intent)
            isServiceRunning.postValue(false)
            permissionUsageData.postValue(arrayListOf())
            Log.d(TAG, "Monitoring service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop monitoring service", e)
        }
    }

    private fun requestServiceUpdate() {
        // Service will broadcast current state automatically
        // If service is not running, start it
        if (isServiceRunning.value != true) {
            startMonitoring()
        }
    }

    private fun handleActiveOpsUpdate(intent: Intent) {
        val activeOps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(
                PermissionMonitorService.EXTRA_ACTIVE_OPS,
                PermissionUsage::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(PermissionMonitorService.EXTRA_ACTIVE_OPS)
        }

        activeOps?.let {
            val filtered = filterPermissionUsage(it, currentKeyword)
            permissionUsageData.postValue(filtered)
            Log.d(TAG, "Received ${it.size} active operations, filtered to ${filtered.size}")
        }
    }

    private fun handleOpChanged(intent: Intent) {
        val opCode = intent.getIntExtra(PermissionMonitorService.EXTRA_OP_CODE, -1)
        val packageName = intent.getStringExtra(PermissionMonitorService.EXTRA_PACKAGE_NAME)
        val active = intent.getBooleanExtra(PermissionMonitorService.EXTRA_ACTIVE, false)

        Log.d(TAG, "Op changed: $opCode - Package: $packageName - Active: $active")

        // Request full update
        requestServiceUpdate()
    }

    private fun filterPermissionUsage(
        usageList: ArrayList<PermissionUsage>,
        keyword: String
    ): ArrayList<PermissionUsage> {
        if (keyword.isEmpty()) return usageList

        val filtered = arrayListOf<PermissionUsage>()
        val lowercaseKeyword = keyword.lowercase()

        for (usage in usageList) {
            val appName = usage.packageInfo.applicationInfo.loadLabel(
                getApplication<Application>().packageManager
            ).toString()
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

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter().apply {
            addAction(PermissionMonitorService.BROADCAST_ACTIVE_OPS)
            addAction(PermissionMonitorService.BROADCAST_OP_CHANGED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(
                broadcastReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            getApplication<Application>().registerReceiver(broadcastReceiver, filter)
        }

        Log.d(TAG, "Broadcast receiver registered")
    }

    private fun unregisterBroadcastReceiver() {
        try {
            getApplication<Application>().unregisterReceiver(broadcastReceiver)
            Log.d(TAG, "Broadcast receiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister broadcast receiver", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterBroadcastReceiver()
        // Don't stop the service here - let the fragment handle that
    }

    companion object {
        private const val TAG = "UsageMonitorViewModel"
    }
}
