package app.simple.inure.services

import android.app.*
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.constants.ServiceConstants
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

    private val batchCopyBinder = BatchCopyBinder()
    private val copyThread = Thread(CopyRunnable())

    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification

    private val notificationId = 123
    private var maxSize = 0L
    private var progress = 0L

    private var channelId = "inure_batch_extract"

    var appsList = arrayListOf<BatchPackageInfo>()
        set(value) {
            field = value
            copyThread.start()
        }

    override fun onBind(intent: Intent?): IBinder {
        return batchCopyBinder
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

    inner class CopyRunnable : Runnable {
        override fun run() {
            measureTotalSize()

            launchOnUiThread {
                createNotification(maxSize)
            }

            var position = 0

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
                            extractBundle(packageInfo = app.packageInfo)
                        } else { // For APK files
                            sendApkTypeBroadcast(APK_TYPE_FILE)
                            extractApk(packageInfo = app.packageInfo)
                        }

                        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionCopyFinished, applicationContext)
                        position++
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

                if (appsList[position - 1].packageInfo.applicationInfo.sourceDir.isNotNull()) {
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
            val source = File(packageInfo.applicationInfo.sourceDir)
            val dest = File(PackageData.getPackageDir(applicationContext), BatchUtils.getApkPathAndFileName(packageInfo))
            val length = source.length()

            inputStream = FileInputStream(source)
            outputStream = FileOutputStream(dest)

            copyStream(inputStream!!, outputStream!!, length)

            inputStream!!.close()
            outputStream!!.close()
        }
    }

    private fun extractBundle(packageInfo: PackageInfo) {
        if (!File(applicationContext.getBundlePathAndFileName(packageInfo)).exists()) {
            launchOnUiThread {
                notificationBuilder.setContentText(packageInfo.applicationInfo.name)
                notificationManager.notify(notificationId, notification)
            }

            val zipFile = ZipFile(applicationContext.getBundlePathAndFileName(packageInfo))
            val progressMonitor = zipFile.progressMonitor
            val length = packageInfo.applicationInfo.splitSourceDirs.getDirectorySize() + packageInfo.applicationInfo.sourceDir.getDirectoryLength()

            zipFile.isRunInThread = true
            zipFile.addFiles(createSplitApkFiles(packageInfo))

            while (!progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                progress += length * (progressMonitor.percentDone / 100)
                notificationBuilder.setProgress(maxSize.toInt(), progress.toInt(), false)
                notificationManager.notify(notificationId, notification)
                sendProgressBroadcast(progressMonitor.percentDone)
                Thread.sleep(100)
            }

            if (progressMonitor.result.equals(ProgressMonitor.Result.ERROR)) {
                println("Error")
                // error.postValue(progressMonitor.exception.stackTraceToString())
            } else if (progressMonitor.result.equals(ProgressMonitor.Result.CANCELLED)) {
                println("Cancelled")
                // status.postValue(getString(R.string.cancelled))
            }
        }
    }

    private fun createSplitApkFiles(packageInfo: PackageInfo): ArrayList<File> {
        val list = arrayListOf<File>()

        list.add(File(packageInfo.applicationInfo.sourceDir))

        for (i in packageInfo.applicationInfo.splitSourceDirs.indices) {
            list.add(File(packageInfo.applicationInfo.splitSourceDirs[i]))
        }

        return list
    }

    @Throws(IOException::class)
    fun copyStream(from: InputStream, to: OutputStream, length: Long) {
        val buf = ByteArray(1024 * 1024)
        var len: Int
        var total = 0L

        while (from.read(buf).also { len = it } > 0) {
            to.write(buf, 0, len)
            total += len
            sendProgressBroadcast((total * 100 / length).toInt())
        }
    }

    private fun measureTotalSize() {
        for (app in appsList) {
            maxSize += File(app.packageInfo.applicationInfo.sourceDir).length()
            if (app.packageInfo.applicationInfo.splitSourceDirs.isNotNull()) {
                maxSize += app.packageInfo.applicationInfo.splitSourceDirs.getDirectorySize()
            }
        }
    }

    /* ----------------------------------------------------------------------------------------------------- */

    private fun sendApkTypeBroadcast(apkType: Int) {
        Intent().also { intent ->
            intent.action = ServiceConstants.actionBatchApkType
            intent.putExtra(APK_TYPE_EXTRA, apkType)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    private fun sendProgressBroadcast(progress: Int) {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionCopyProgress, applicationContext, progress)
    }

    private fun sendMaxProgress(max: Int) {
        Intent().also { intent ->
            intent.action = ServiceConstants.actionCopyProgressMax
            intent.putExtra(IntentHelper.INT_EXTRA, max)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    /* ----------------------------------------------------------------------------------------------------- */

    private fun createNotification(maxProgress: Long) {
        createNotificationChannel()

        // Create an explicit intent for an Activity in your app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        notificationBuilder.setContentTitle(getString(R.string.extract))
            .setContentText("Extracting apps")
            .setSmallIcon(R.drawable.ic_downloading)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setShowWhen(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .addAction(generateAction(R.drawable.ic_close, getString(R.string.cancel), ServiceConstants.actionBatchCancel))
            .setProgress(maxProgress.toInt(), 0, false)

        notification = notificationBuilder.build()
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

    private fun generateAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        val intent = Intent(this, BatchExtractService::class.java)
        intent.action = action
        val close = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action.Builder(icon, title, close).build()
    }

    inner class BatchCopyBinder : Binder() {
        fun getService(): BatchExtractService {
            return this@BatchExtractService
        }
    }

    companion object {
        const val APK_TYPE_SPLIT = 1
        const val APK_TYPE_FILE = 2
        const val CREATING_SPLIT_PACKAGE = 3

        const val APK_TYPE_EXTRA = "APK_TYPE_EXTRA"
    }
}