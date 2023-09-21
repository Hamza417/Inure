package app.simple.inure.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.math.Extensions.percentOf
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.util.BatchUtils
import app.simple.inure.util.BatchUtils.getBundlePathAndFileName
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.areStoragePermissionsGranted
import com.anggrayudi.storage.extension.launchOnUiThread
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.*

class BatchExtractService : Service() {

    private val batchExtractServiceBinder = BatchExtractServiceBinder()
    private val copyThread = Thread(CopyRunnable())

    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification

    private val notificationId = 123
    private var progress = 0L
    private var channelId = "inure_batch_extract"

    internal var maxSize = 0L
    internal var position = 0
    internal var apkType = 0

    private var appsList = arrayListOf<BatchPackageInfo>()

    override fun onBind(intent: Intent?): IBinder {
        return batchExtractServiceBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ServiceConstants.actionBatchCancel -> {
                copyThread.interrupt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        SharedPreferences.init(applicationContext)
        notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            copyThread.interrupt()
        } catch (e: IOException) {
            /**
             * Failed to close streams
             */
            e.printStackTrace()
        }
    }

    internal fun startCopying(appsList: ArrayList<BatchPackageInfo>, function: () -> Unit) {
        Log.d("BatchExtractService", "startCopying: ${appsList.size}")
        this.appsList.addAll(appsList)
        copyThread.start()
        function()
    }

    fun getAppList(): ArrayList<BatchPackageInfo> {
        return appsList
    }

    inner class CopyRunnable : Runnable {
        override fun run() {
            measureTotalSize()

            launchOnUiThread {
                createNotification()
            }

            try {
                for (app in appsList) {
                    try {
                        if (applicationContext.areStoragePermissionsGranted()) {
                            PackageData.makePackageFolder(applicationContext)
                        } else {
                            notificationBuilder.setContentText(getString(R.string.grant_storage_access_permission))
                            throw SecurityException("Storage Permission not granted")
                        }

                        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionBatchCopyStart, applicationContext, position)

                        if (app.packageInfo.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                            sendApkTypeBroadcast(APK_TYPE_SPLIT)
                            apkType = APK_TYPE_SPLIT
                            extractBundle(packageInfo = app.packageInfo)
                        } else { // For APK files
                            sendApkTypeBroadcast(APK_TYPE_FILE)
                            apkType = APK_TYPE_FILE
                            extractApk(packageInfo = app.packageInfo)
                        }

                        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionCopyFinished, applicationContext)
                        position++

                        if (Thread.currentThread().isInterrupted) {
                            throw InterruptedException("Thread interrupted")
                        }
                    } catch (e: SecurityException) {
                        /**
                         * Terminate the process since the permission is
                         * not granted, file cannot be copied
                         */
                        e.printStackTrace()
                    } catch (e: NullPointerException) {
                        /**
                         * File does not exit
                         */
                        e.printStackTrace()
                    } catch (e: IOException) {
                        /**
                         * Some IO error happened, skip this app
                         * and flush the buffer
                         */
                        e.printStackTrace()
                    }
                }
            } catch (e: InterruptedException) {
                /**
                 * Thread has been interrupted
                 * stop the service
                 */
                e.printStackTrace()

                if (appsList[position].packageInfo.applicationInfo.sourceDir.isNotNull()) {
                    File(applicationContext.getBundlePathAndFileName(appsList[position].packageInfo)).delete()
                } else {
                    File(BatchUtils.getApkPathAndFileName(appsList[position].packageInfo)).delete()
                }

                launchOnUiThread {
                    notificationManager.cancel(notificationId)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }

                    stopSelf()
                }
            }

            Intent().also { intent ->
                intent.action = ServiceConstants.actionExtractDone
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }

            launchOnUiThread {
                notificationManager.cancel(notificationId)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            stopSelf()
        }
    }

    private fun extractApk(packageInfo: PackageInfo) {
        if (File(PackageData.getPackageDir(applicationContext), BatchUtils.getApkPathAndFileName(packageInfo)).exists().invert()) {
            notificationBuilder.setContentText(packageInfo.applicationInfo.name)
            val source = File(packageInfo.applicationInfo.sourceDir)
            val dest = File(PackageData.getPackageDir(applicationContext), BatchUtils.getApkPathAndFileName(packageInfo))

            inputStream = FileInputStream(source)
            outputStream = FileOutputStream(dest)

            copyStream(inputStream!!, outputStream!!)

            inputStream!!.close()
            outputStream!!.close()
        } else {
            progress += File(packageInfo.applicationInfo.sourceDir).length()
            sendProgressBroadcast(progress)
        }
    }

    private fun extractBundle(packageInfo: PackageInfo) {
        if (!File(applicationContext.getBundlePathAndFileName(packageInfo)).exists()) {
            launchOnUiThread {
                notificationBuilder.setContentText(packageInfo.applicationInfo.name)
                if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return@launchOnUiThread
                }
                notificationManager.notify(notificationId, notificationBuilder.build())
            }

            val zipFile = ZipFile(applicationContext.getBundlePathAndFileName(packageInfo))
            val progressMonitor = zipFile.progressMonitor
            var oldProgress = 0L

            zipFile.isRunInThread = true
            zipFile.addFiles(createSplitApkFiles(packageInfo))

            while (!progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                // Calculate progress and show it on notification
                val newProgress = progressMonitor.workCompleted
                progress += newProgress - oldProgress
                oldProgress = newProgress

                launchOnUiThread {
                    notificationBuilder.setProgress(100, progress.percentOf(maxSize).toInt(), false)
                    if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notificationManager.notify(notificationId, notificationBuilder.build())
                    }
                    sendProgressBroadcast(progress)
                }

                /**
                 * This will cause a lot of progress updates, but it's ok
                 * Not keeping this will make the progress bar stuck at 0%
                 * for a long time
                 */
                Thread.sleep(100)
            }
        } else {
            progress += (packageInfo.applicationInfo.splitSourceDirs?.getDirectorySize() ?: 0) + packageInfo.applicationInfo.sourceDir.getDirectoryLength()
            sendProgressBroadcast(progress)
        }
    }

    private fun createSplitApkFiles(packageInfo: PackageInfo): ArrayList<File> {
        val list = arrayListOf<File>()

        list.add(File(packageInfo.applicationInfo.sourceDir))

        for (i in packageInfo.applicationInfo.splitSourceDirs?.indices!!) {
            list.add(File(packageInfo.applicationInfo.splitSourceDirs!![i]))
        }

        return list
    }

    @Throws(IOException::class)
    fun copyStream(from: InputStream, to: OutputStream) {
        val buf = ByteArray(1024 * 1024)
        var len: Long

        while (from.read(buf).also { len = it.toLong() } > 0) {
            to.write(buf, 0, len.toInt())
            progress += len
            launchOnUiThread {
                notificationBuilder.setProgress(100, (progress.toDouble() / maxSize.toDouble() * 100).toInt(), false)
                if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notificationId, notificationBuilder.build())
                }
                sendProgressBroadcast(progress)
            }
        }
    }

    private fun measureTotalSize() {
        for (app in appsList) {
            maxSize += File(app.packageInfo.applicationInfo.sourceDir).length()

            if (app.packageInfo.applicationInfo.splitSourceDirs.isNotNull()) {
                maxSize += app.packageInfo.applicationInfo.splitSourceDirs?.getDirectorySize() ?: 0L
            }
        }

        sendMaxProgress(maxSize)
    }

    /* ----------------------------------------------------------------------------------------------------- */

    private fun sendApkTypeBroadcast(apkType: Int) {
        Intent().also { intent ->
            intent.action = ServiceConstants.actionBatchApkType
            intent.putExtra(APK_TYPE_EXTRA, apkType)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    private fun sendProgressBroadcast(progress: Long) {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionCopyProgress, applicationContext, progress)
    }

    private fun sendMaxProgress(max: Long) {
        Intent().also { intent ->
            intent.action = ServiceConstants.actionCopyProgressMax
            intent.putExtra(IntentHelper.INT_EXTRA, max)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    /* ----------------------------------------------------------------------------------------------------- */

    private fun createNotification() {
        createNotificationChannel()

        // Create an explicit intent for an Activity in your app
        val pendingIntent = with(Intent(this, MainActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.action = ShortcutConstants.BATCH_EXTRACT_ACTION
            PendingIntent.getActivity(applicationContext, 111, this, PendingIntent.FLAG_IMMUTABLE)
        }

        notificationBuilder.setContentTitle(getString(R.string.extracting))
            .setSmallIcon(R.drawable.ic_downloading)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setShowWhen(true)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(generateAction(R.drawable.ic_close, getString(R.string.cancel), ServiceConstants.actionBatchCancel))
            .setProgress(100, 0, false)

        notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.batch)
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(false)
            channel.enableLights(false)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Suppress("SameParameterValue")
    private fun generateAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        val intent = Intent(this, BatchExtractService::class.java)
        intent.action = action
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    fun reshowNotification() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        notificationManager.notify(notificationId, notification)
    }

    fun interruptCopying() {
        try {
            copyThread.interrupt()
        } catch (e: IllegalStateException) {
            Log.e("BatchExtractService", "Thread is not running")
        }
    }

    inner class BatchExtractServiceBinder : Binder() {
        fun getService(): BatchExtractService {
            return this@BatchExtractService
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BatchExtractService::class.java)
        }

        const val APK_TYPE_SPLIT = 1
        const val APK_TYPE_FILE = 2

        @Suppress("unused")
        const val CREATING_SPLIT_PACKAGE = 3

        const val APK_TYPE_EXTRA = "APK_TYPE_EXTRA"
    }
}