package app.simple.inure.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.util.BatchUtils
import app.simple.inure.util.BatchUtils.getBundlePathAndFileName
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.areStoragePermissionsGranted
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.*

class BatchExtractService : Service() {

    private val batchCopyBinder = BatchCopyBinder()
    private val copyThread = Thread(CopyRunnable())

    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    var appsList = arrayListOf<BatchPackageInfo>()
        set(value) {
            field = value
            copyThread.start()
        }

    override fun onBind(intent: Intent?): IBinder {
        return batchCopyBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
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
            for (app in appsList) {
                try {
                    if (applicationContext.areStoragePermissionsGranted()) {
                        PackageData.makePackageFolder(applicationContext)
                    } else {
                        throw SecurityException("Storage Permission not granted")
                    }

                    if (app.packageInfo.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                        sendApkTypeBroadcast(APK_TYPE_SPLIT)
                        extractBundle(packageInfo = app.packageInfo)
                    } else { // For APK files
                        sendApkTypeBroadcast(APK_TYPE_FILE)
                        extractApk(packageInfo = app.packageInfo)
                    }
                } catch (e: SecurityException) {
                    /**
                     * Terminate the process since the permission is
                     * not granted, file cannot be copied
                     */
                    e.printStackTrace()
                    break
                } catch (e: NullPointerException) {
                    /**
                     * File does not exit
                     */
                    e.printStackTrace()
                } catch (e: IOException) {
                    /**
                     * Some IO error happened, skip this apk
                     * and flush the buffer
                     */
                    e.printStackTrace()
                } finally {
                    try {

                    } catch (e: IOException) {
                        /**
                         * Failed to close streams
                         */
                        e.printStackTrace()
                    }
                }
            }
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
        kotlin.runCatching {
            if (!File(applicationContext.getBundlePathAndFileName(packageInfo)).exists()) {
                // status.postValue(getString(R.string.creating_split_package))
                val zipFile = ZipFile(applicationContext.getBundlePathAndFileName(packageInfo))
                val progressMonitor = zipFile.progressMonitor

                zipFile.isRunInThread = true
                zipFile.addFiles(createSplitApkFiles(packageInfo))

                while (!progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                    sendProgressBroadcast(progressMonitor.percentDone.toLong())
                }

                if (progressMonitor.result.equals(ProgressMonitor.Result.ERROR)) {
                    println("Error")
                    // error.postValue(progressMonitor.exception.stackTraceToString())
                } else if (progressMonitor.result.equals(ProgressMonitor.Result.CANCELLED)) {
                    println("Cancelled")
                    // status.postValue(getString(R.string.cancelled))
                }
            }
        }.onFailure {
            it.printStackTrace()
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
            sendProgressBroadcast(total * 100 / length)
        }
    }

    /* ----------------------------------------------------------------------------------------------------- */

    private fun sendApkTypeBroadcast(apkType: Int) {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionApkType, applicationContext, apkType)
    }

    private fun sendProgressBroadcast(progress: Long) {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionApkType, applicationContext, progress)
    }

    /* ----------------------------------------------------------------------------------------------------- */

    inner class BatchCopyBinder : Binder() {
        fun getService(): BatchExtractService {
            return this@BatchExtractService
        }
    }

    companion object {
        const val APK_TYPE_SPLIT = 1
        const val APK_TYPE_FILE = 2
        const val CREATING_SPLIT_PACKAGE = 3
    }
}