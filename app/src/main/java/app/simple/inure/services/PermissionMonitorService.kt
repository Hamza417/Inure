package app.simple.inure.services

import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.models.AppOpsMonitor
import app.simple.inure.models.PermissionUsage
import java.util.concurrent.ConcurrentHashMap

/**
 * Background service for real-time AppOps monitoring
 * Uses AppOpsManager.OnOpActiveChangedListener for live callbacks
 */
class PermissionMonitorService : Service() {

    private var appOpsManager: AppOpsManager? = null
    private val activeOpsMap = ConcurrentHashMap<String, MutableSet<Int>>() // packageName -> Set<opCode>
    private val listeners = mutableListOf<AppOpsManager.OnOpActiveChangedListener>()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        appOpsManager = getSystemService(AppOpsManager::class.java)

        // Create notification channel
        createNotificationChannel()

        // Start as foreground service
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Register listeners for all monitored operations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerOpListeners()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        intent?.let {
            when (it.action) {
                ACTION_START_MONITORING -> {
                    // Already started in onCreate
                    broadcastActiveOps()
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

        // Unregister all listeners
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            unregisterOpListeners()
        }

        // Clear data
        activeOpsMap.clear()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun registerOpListeners() {
        val opsToWatch = AppOpsMonitor.getAllMonitoredOps()
        Log.d(TAG, "Registering listeners for ${opsToWatch.size} operations")

        for (op in opsToWatch) {
            try {
                val listener = createListenerForOp(op)
                appOpsManager?.startWatchingActive(intArrayOf(op), { /* executor */ }, listener)
                listeners.add(listener)
                Log.d(TAG, "Registered listener for op: ${AppOpsMonitor.opToName(op)} ($op)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register listener for op $op", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun unregisterOpListeners() {
        Log.d(TAG, "Unregistering ${listeners.size} listeners")

        for (listener in listeners) {
            try {
                appOpsManager?.stopWatchingActive(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister listener", e)
            }
        }

        listeners.clear()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createListenerForOp(opCode: Int): AppOpsManager.OnOpActiveChangedListener {
        return AppOpsManager.OnOpActiveChangedListener { op, uid, packageName, active ->
            handleOpActiveChanged(op, uid, packageName, active)
        }
    }

    private fun handleOpActiveChanged(op: Int, uid: Int, packageName: String, active: Boolean) {
        val opName = AppOpsMonitor.opToName(op)
        Log.d(TAG, "Op changed: $opName ($op) - Package: $packageName - Active: $active")

        synchronized(activeOpsMap) {
            if (active) {
                activeOpsMap.getOrPut(packageName) { mutableSetOf() }.add(op)
            } else {
                activeOpsMap[packageName]?.remove(op)
                if (activeOpsMap[packageName]?.isEmpty() == true) {
                    activeOpsMap.remove(packageName)
                }
            }
        }

        // Broadcast the change
        broadcastOpChange(op, uid, packageName, active)
        broadcastActiveOps()
    }

    private fun broadcastOpChange(op: Int, uid: Int, packageName: String, active: Boolean) {
        val intent = Intent(BROADCAST_OP_CHANGED).apply {
            putExtra(EXTRA_OP_CODE, op)
            putExtra(EXTRA_UID, uid)
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            putExtra(EXTRA_ACTIVE, active)
            putExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
        }
        sendBroadcast(intent)
    }

    private fun broadcastActiveOps() {
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

        val intent = Intent(BROADCAST_ACTIVE_OPS).apply {
            putParcelableArrayListExtra(EXTRA_ACTIVE_OPS, activeList)
        }
        sendBroadcast(intent)
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
