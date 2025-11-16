package app.simple.inure.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import app.simple.inure.IAppOpsActiveCallback
import app.simple.inure.IUserService
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.AppOpsMonitor
import app.simple.inure.models.PermissionUsage
import app.simple.inure.models.PermissionMonitorRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * Background service for real-time AppOps monitoring
 * Uses Shizuku UserService with system privileges to monitor AppOps
 */
class PermissionMonitorService : Service() {

    private var shizukuServiceHelper: ShizukuServiceHelper? = null
    private var userService: IUserService? = null
    private val activeOpsMap = ConcurrentHashMap<String, MutableSet<Int>>() // packageName -> Set<opCode>
    private var appOpsCallback: IAppOpsActiveCallback? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== SERVICE LOGGING VERSION 2025-11-17-B CREATED ===")

        // Update repository monitoring status
        PermissionMonitorRepository.setMonitoring(true)

        // Create notification channel
        createNotificationChannel()

        // Start as foreground service
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Bind to Shizuku service and register listeners
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bindShizukuService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun bindShizukuService() {
        try {
            shizukuServiceHelper = ShizukuServiceHelper.getInstance()
            shizukuServiceHelper?.bindUserService {
                userService = shizukuServiceHelper?.service
                if (userService != null) {
                    Log.d(TAG, "Shizuku service bound successfully")
                    registerOpListeners()
                } else {
                    Log.e(TAG, "Failed to get Shizuku user service")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind Shizuku service", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        intent?.let {
            when (it.action) {
                ACTION_START_MONITORING -> {
                    // Update repository with current state
                    updateRepository()
                }
                ACTION_STOP_MONITORING -> {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        // Update repository monitoring status
        PermissionMonitorRepository.setMonitoring(false)
        PermissionMonitorRepository.clear()

        // Unregister all listeners
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            unregisterOpListeners()
        }

        // Unbind Shizuku service
        try {
            shizukuServiceHelper?.unbindUserService()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind Shizuku service", e)
        }

        // Clear data
        activeOpsMap.clear()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun registerOpListeners() {
        val opsToWatch = AppOpsMonitor.getAllMonitoredOps()
        Log.d(TAG, "Registering listeners for ${opsToWatch.size} operations via Shizuku")

        try {
            // Create the AIDL callback stub
            appOpsCallback = object : IAppOpsActiveCallback.Stub() {
                override fun onOpActiveChanged(op: Int, uid: Int, packageName: String?, attributionTag: String?, active: Boolean) {
                    packageName?.let {
                        handleOpActiveChanged(op, uid, it, active)
                    }
                }
            }

            // Call the Shizuku service to start watching (opsToWatch is already IntArray)
            userService?.startWatchingActive(opsToWatch, appOpsCallback)
            Log.d(TAG, "Successfully registered ${opsToWatch.size} listeners via Shizuku")
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to register AppOps listeners via Shizuku", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error registering listeners", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun unregisterOpListeners() {
        Log.d(TAG, "Unregistering listeners via Shizuku")

        try {
            appOpsCallback?.let { callback ->
                userService?.stopWatchingActive(callback)
                appOpsCallback = null
                Log.d(TAG, "Successfully unregistered listeners")
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister listeners via Shizuku", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error unregistering listeners", e)
        }
    }

    private fun handleOpActiveChanged(op: Int, uid: Int, packageName: String, active: Boolean) {
        try {
            val opName = AppOpsMonitor.opToName(op)
            val permissionId = AppOpsMonitor.opToPermission(op)
            Log.d(TAG, "Op changed: $opName ($op) - Package: $packageName - Active: $active")

            // Add or update the permission in the log
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val usage = PermissionUsage(
                    packageInfo = packageInfo,
                    permission = opName,
                    permissionId = permissionId,
                    isEnabled = true,
                    lastAccessTime = System.currentTimeMillis(),
                    duration = null,
                    rejectTime = null,
                    isActive = active
                )

                // Add to repository log (bubbles to top when active)
                PermissionMonitorRepository.addOrUpdatePermission(usage)
                Log.d(TAG, "Added/Updated permission log for $packageName:$permissionId, active=$active")

            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Package not found: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleOpActiveChanged", e)
        }
    }

    private fun updateRepository() {
        val activeList = ArrayList<PermissionUsage>()

        synchronized(activeOpsMap) {
            for ((packageName, ops) in activeOpsMap) {
                try {
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)

                    for (op in ops) {
                        val usage = PermissionUsage(
                            packageInfo = packageInfo,
                            permission = AppOpsMonitor.opToName(op),
                            permissionId = AppOpsMonitor.opToPermission(op),
                            isEnabled = true,
                            lastAccessTime = System.currentTimeMillis(),
                            duration = null,
                            rejectTime = null,
                            isActive = true
                        )
                        activeList.add(usage)
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "Package not found: $packageName")
                }
            }
        }

        Log.d(TAG, "Updating repository with ${activeList.size} items")
        PermissionMonitorRepository.updateActivePermissions(activeList)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.permission_monitor),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Real-time permission usage monitoring"
                setShowBadge(false)
                setSound(null, null)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.permission_monitor))
            .setContentText("Monitoring ${activeOpsMap.size} apps")
            .setSmallIcon(R.drawable.ic_visibility)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "PermissionMonitorService"
        private const val CHANNEL_ID = "permission_monitor_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START_MONITORING = "app.simple.inure.START_MONITORING"
        const val ACTION_STOP_MONITORING = "app.simple.inure.STOP_MONITORING"

        const val BROADCAST_OP_CHANGED = "app.simple.inure.OP_CHANGED"
        const val BROADCAST_ACTIVE_OPS = "app.simple.inure.ACTIVE_OPS"

        const val EXTRA_OP_CODE = "op_code"
        const val EXTRA_UID = "uid"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_ACTIVE = "active"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_ACTIVE_OPS = "active_ops"
    }
}
