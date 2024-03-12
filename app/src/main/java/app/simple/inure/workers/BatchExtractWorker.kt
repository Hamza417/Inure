package app.simple.inure.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.math.Extensions.percentOf
import app.simple.inure.services.BatchExtractService
import app.simple.inure.util.BatchUtils
import app.simple.inure.util.BatchUtils.getBundlePathAndFileName
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.areStoragePermissionsGranted
import com.anggrayudi.storage.extension.launchOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BatchExtractWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification

    private val notificationId = 123
    private var progress = 0L
    private var channelId = "inure_batch_extract"
    private var isExtracting = false

    private var maxSize = 0L
    internal var position = 0
    private var apkType = 0

    private var apps = arrayListOf<PackageInfo>()

    override suspend fun doWork(): Result {
        notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)

        return withContext(Dispatchers.IO) {
            isExtracting = true

            initializeAppsList() // Initialize list of apps
            measureTotalSize() // Measure total size of all apps
            createNotification() // Create notification and start foreground service

            Log.d(TAG, "doWork: Extracting ${apps.size} apps")
            // Start copying
            try {
                for (app in apps) {
                    Log.d(TAG, "doWork: Extracting ${app.applicationInfo.name}")
                    try {
                        if (applicationContext.areStoragePermissionsGranted()) {
                            PackageData.makePackageFolder(applicationContext)
                        } else {
                            notificationBuilder.setContentText(applicationContext.getString(R.string.grant_storage_access_permission))
                            throw SecurityException("Storage Permission not granted")
                        }

                        setProgress(workDataOf(POSITION to position.plus(1)))

                        if (app.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                            apkType = APK_TYPE_SPLIT
                            setProgress(workDataOf(APK_TYPE to APK_TYPE_SPLIT))
                            extractBundle(packageInfo = app)
                        } else { // For APK files
                            apkType = APK_TYPE_FILE
                            setProgress(workDataOf(APK_TYPE to APK_TYPE_FILE))
                            extractApk(packageInfo = app)
                        }

                        setProgress(workDataOf(COPY_FINISHED to true))
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
                        isExtracting = false
                        launchOnUiThread {
                            notificationManager.cancel(notificationId)
                        }
                        return@withContext Result.failure()
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
                isExtracting = false

                if (apps[position].applicationInfo.splitSourceDirs.isNotNull()) {
                    File(applicationContext.getBundlePathAndFileName(apps[position])).delete()
                } else {
                    File(BatchUtils.getApkPathAndFileName(apps[position])).delete()
                }

                launchOnUiThread {
                    notificationManager.cancel(notificationId)
                }

                return@withContext Result.failure()
            }

            isExtracting = false
            setProgress(workDataOf(EXTRACT_DONE to true))
            resetState()

            launchOnUiThread {
                notificationManager.cancel(notificationId)
            }

            return@withContext Result.success()
        }
    }

    private fun measureTotalSize() {
        for (app in apps) {
            maxSize += File(app.applicationInfo.sourceDir).length()

            if (app.applicationInfo.splitSourceDirs.isNotNull()) {
                maxSize += app.applicationInfo.splitSourceDirs?.getDirectorySize() ?: 0L
            }
        }

        workDataOf(COPY_PROGRESS_MAX to maxSize)
    }

    private fun initializeAppsList() {
        val list = inputData.getStringArray(APP_DATA) ?: throw NullPointerException("App data is null")
        if (list.isNotNull()) {
            for (i in list.indices) {
                val packageInfo = applicationContext.packageManager.getPackageInfo(list[i], PackageManager.GET_META_DATA)
                packageInfo.applicationInfo.name = applicationContext.packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
                apps.add(packageInfo)
            }
        }
    }

    fun resetState() {
        progress = 0L
        maxSize = 0L
        position = 0
        apkType = 0
    }

    private fun extractApk(packageInfo: PackageInfo) {
        notificationBuilder.setContentText("(${position.plus(1)}/${apps.size}) ${packageInfo.applicationInfo.name}_${packageInfo.versionName}.apk")

        if (File(PackageData.getPackageDir(applicationContext), BatchUtils.getApkPathAndFileName(packageInfo)).exists().invert()) {
            val source = File(packageInfo.applicationInfo.sourceDir)
            val dest = File(PackageData.getPackageDir(applicationContext), BatchUtils.getApkPathAndFileName(packageInfo))

            inputStream = FileInputStream(source)
            outputStream = FileOutputStream(dest)

            copyStream(inputStream!!, outputStream!!)

            inputStream!!.close()
            outputStream!!.close()
        } else {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, notificationBuilder.build())
            }

            progress += File(packageInfo.applicationInfo.sourceDir).length()
            workDataOf(COPY_PROGRESS to progress)
        }
    }

    private fun extractBundle(packageInfo: PackageInfo) {
        notificationBuilder.setContentText("(${position.plus(1)}/${apps.size}) ${packageInfo.applicationInfo.name}_${packageInfo.versionName}.apks")
        if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }

        if (!File(applicationContext.getBundlePathAndFileName(packageInfo)).exists()) {
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
                    if (ActivityCompat.checkSelfPermission(
                                    applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notificationManager.notify(notificationId, notificationBuilder.build())
                    }

                    workDataOf(COPY_PROGRESS to progress)
                }

                /**
                 * This will cause a lot of progress updates, but it's ok
                 * Not keeping this will make the progress bar stuck at 0%
                 * for a long time
                 */
                Thread.sleep(100)
            }
        } else {
            progress += (packageInfo.applicationInfo.splitSourceDirs?.getDirectorySize() ?: 0) +
                    packageInfo.applicationInfo.sourceDir.getDirectoryLength()

            workDataOf(COPY_PROGRESS to progress)
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
            notificationBuilder.setProgress(100, (progress.toDouble() / maxSize.toDouble() * 100).toInt(), false)
            if (ActivityCompat.checkSelfPermission(
                            applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, notificationBuilder.build())
            }

            workDataOf(COPY_PROGRESS to progress)
        }
    }

    private fun createNotification() {
        createNotificationChannel()

        // Create an explicit intent for an Activity in your app
        val pendingIntent = with(Intent(this.applicationContext, MainActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.action = ShortcutConstants.BATCH_EXTRACT_ACTION
            PendingIntent.getActivity(applicationContext, 111, this, PendingIntent.FLAG_IMMUTABLE)
        }

        notificationBuilder.setContentTitle(applicationContext.getString(R.string.extracting))
            .setSmallIcon(R.drawable.ic_downloading)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setShowWhen(true)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(generateAction(R.drawable.ic_close, applicationContext.getString(R.string.cancel), ServiceConstants.actionBatchCancel))
            .setProgress(100, 0, false)

        notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = this.applicationContext.getString(R.string.extracting)
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(false)
            channel.enableLights(false)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Suppress("SameParameterValue")
    private fun generateAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        val intent = Intent(this.applicationContext, BatchExtractService::class.java)
        intent.action = action
        val pendingIntent = PendingIntent.getService(this.applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    fun reshowNotification() {
        if (ActivityCompat.checkSelfPermission(this.applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val APK_TYPE = "apk_type"
        const val POSITION = "position"
        const val COPY_FINISHED = "copy_finished"
        const val COPY_PROGRESS = "copy_progress"
        const val COPY_PROGRESS_MAX = "copy_progress_max"
        const val EXTRACT_DONE = "extract_done"
        const val APP_DATA = "app_data"
        const val APK_TYPE_SPLIT = 1
        const val APK_TYPE_FILE = 0

        val WORK_UUID = UUID.fromString("f3e3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e")
        val TAG = "BatchExtractWorker"
    }
}
