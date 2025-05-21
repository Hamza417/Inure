package app.simple.inure.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.utils.JsonParserUtil
import app.simple.inure.virustotal.VirusTotalClient
import app.simple.inure.virustotal.VirusTotalResponse
import app.simple.inure.virustotal.VirusTotalResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class VirusTotalClientService : Service() {

    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var uploadJob: Job? = null

    private val _progressFlow = MutableSharedFlow<VirusTotalResult.Progress>(replay = 1)
    private val _failedFlow = MutableSharedFlow<VirusTotalResult.Error>(replay = 1)
    private val _successFlow = MutableSharedFlow<VirusTotalResponse>(replay = 1)
    private val _warningFlow = MutableSharedFlow<String>(replay = 1)
    private val _exitFlow = MutableSharedFlow<Unit>(replay = 1)

    val progressFlow = _progressFlow.asSharedFlow()
    val successFlow = _successFlow.asSharedFlow()
    val failedFlow = _failedFlow.asSharedFlow()
    val warningFlow = _warningFlow.asSharedFlow()
    val exitFlow = _exitFlow.asSharedFlow()

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification

    @Volatile
    private var isScanning = true

    @Volatile
    private var lastPackageName = ""

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): VirusTotalClientService = this@VirusTotalClientService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ServiceConstants.ACTION_VIRUS_TOTAL_CANCEL) {
            scope.launch {
                _exitFlow.emit(Unit)
            }
            uploadJob?.cancel()
            isScanning = false
            lastPackageName = ""
            notificationManager.cancel(NOTIFICATION_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        SharedPreferences.init(applicationContext)
        notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearEverything(packageInfo: PackageInfo) {
        if (packageInfo.packageName != lastPackageName) {
            isScanning = false
            lastPackageName = ""
            uploadJob?.cancel()
            notificationManager.cancel(NOTIFICATION_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            _progressFlow.resetReplayCache()
            _failedFlow.resetReplayCache()
            _successFlow.resetReplayCache()
            _warningFlow.resetReplayCache()
        }
    }

    fun startUpload(packageInfo: PackageInfo) {
        Log.d(TAG, lastPackageName)
        if (isScanning && packageInfo.packageName == lastPackageName) {
            Log.w(TAG, "Already scanning")
            return
        }

        isScanning = true
        lastPackageName = packageInfo.packageName
        uploadJob?.cancel()

        uploadJob = scope.launch {
            withContext(Dispatchers.Main) {
                createNotification()
            }

            try {
                VirusTotalClient.getInstance().scanFile(packageInfo.safeApplicationInfo.sourceDir).collect { response ->
                    when (response) {
                        is VirusTotalResult.Error -> {
                            _failedFlow.emit(response)
                            withContext(Dispatchers.Main) {
                                notificationManager.cancel(NOTIFICATION_ID)
                                notificationBuilder.setContentText(getString(R.string.error))
                                    .setProgress(0, 0, false)
                                    .setOngoing(false)
                                notification = notificationBuilder.build()
                                if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    notificationManager.notify(NOTIFICATION_ID, notification)
                                }
                            }
                        }
                        is VirusTotalResult.Progress -> {
                            _progressFlow.emit(response)

                            when (response.progressCode) {
                                VirusTotalResult.Progress.UPLOADING -> {
                                    withContext(Dispatchers.Main) {
                                        notificationBuilder.setContentText(getString(R.string.uploading_file, response.progress))
                                            .setProgress(100, response.progress, false)
                                        notification = notificationBuilder.build()
                                        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                                                == PackageManager.PERMISSION_GRANTED) {
                                            notificationManager.notify(NOTIFICATION_ID, notification)
                                        }
                                    }
                                }
                                VirusTotalResult.Progress.UPLOAD_SUCCESS -> {
                                    withContext(Dispatchers.Main) {
                                        notificationBuilder.setContentText(getString(R.string.done))
                                            .setProgress(0, 0, false)
                                        notification = notificationBuilder.build()
                                        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                                                == PackageManager.PERMISSION_GRANTED) {
                                            notificationManager.notify(NOTIFICATION_ID, notification)
                                        }
                                    }
                                }
                                VirusTotalResult.Progress.POLLING -> {
                                    withContext(Dispatchers.Main) {
                                        notificationBuilder.setContentText(getString(R.string.polling_for_response, response.pollingAttempts))
                                            .setProgress(0, 0, false)
                                        notification = notificationBuilder.build()
                                        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                                                == PackageManager.PERMISSION_GRANTED) {
                                            notificationManager.notify(NOTIFICATION_ID, notification)
                                        }
                                    }
                                }
                            }
                        }
                        is VirusTotalResult.Success -> {
                            handleResponse(response.result)?.let {
                                _successFlow.emit(it)
                            }
                        }
                        is VirusTotalResult.Uploaded -> {
                            // We don't need to show analysis ID in the UI
                            Log.i(TAG, "Uploaded: ${response.result}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed: ${e.message}")
                _warningFlow.emit(e.message ?: "Unknown error")
            } finally {
                isScanning = false
                lastPackageName = ""
                uploadJob = null
                withContext(Dispatchers.Main) {
                    notificationManager.cancel(NOTIFICATION_ID)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                }
            }
        }
    }

    private fun handleResponse(jsonObject: JSONObject?): VirusTotalResponse? {
        return JsonParserUtil.parseSingleAttributes(jsonObject, VirusTotalResponse::class.java)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(false)
            channel.enableLights(false)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        createNotificationChannel()

        notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        notificationBuilder.setContentTitle(getString(R.string.virustotal))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setShowWhen(true)
            .setSilent(true)
            .setOngoing(true)
            .addAction(generateAction(R.drawable.ic_close, getString(R.string.cancel), ServiceConstants.ACTION_VIRUS_TOTAL_CANCEL))
            .setProgress(100, 0, false)

        notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT

        if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        notificationManager.notify(NOTIFICATION_ID, notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @Suppress("SameParameterValue")
    private fun generateAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        val intent = Intent(this, VirusTotalClientService::class.java)
        intent.action = action
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadJob?.cancel()
        scope.cancel()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, VirusTotalClientService::class.java)
        }

        private const val TAG = "VirusTotalClientService"
        private const val CHANNEL_ID = "virustotal_upload"
        private const val CHANNEL_NAME = "VirusTotal Upload"
        private const val NOTIFICATION_ID = 6541687
    }
}