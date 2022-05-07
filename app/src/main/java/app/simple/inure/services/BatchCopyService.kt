package app.simple.inure.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.PermissionUtils.arePermissionsGranted
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BatchCopyService : Service() {

    private val batchCopyBinder = BatchCopyBinder()
    private val copyThread = Thread(CopyRunnable())

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var bufferedInputStream: BufferedInputStream? = null
    private var bufferedOutputStream: BufferedOutputStream? = null
    private var fileInputStream: FileInputStream? = null
    private var zipOutputStream: ZipOutputStream? = null
    private var contentResolverOutputStream: OutputStream? = null

    var appsList = arrayListOf<PackageInfo>()
        set(value) {
            field = value
            copyThread.start()
        }

    private val buffer = 2048
    private val byteArray = ByteArray(buffer)

    override fun onBind(intent: Intent?): IBinder {
        return batchCopyBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bufferedInputStream?.close()
            fileInputStream?.close()
            zipOutputStream?.close()
            bufferedOutputStream?.close()
            contentResolverOutputStream?.close()
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
                    if (!applicationContext.arePermissionsGranted(MainPreferences.getStoragePermissionUri())) {
                        throw SecurityException("Storage Permission not granted")
                    }

                    val fileName = app.applicationInfo.name + " (" + app.versionName + ")"
                    val uri = Uri.parse(MainPreferences.getStoragePermissionUri())
                    val pickedDir = DocumentFile.fromTreeUri(application, uri)
                    var total = 0L
                    var length = File(app.applicationInfo.sourceDir).length()

                    if (app.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                        sendApkTypeBroadcast(APK_TYPE_SPLIT)

                        val documentFile = pickedDir!!.createFile("application/zip", fileName)
                        val listOfSplitFiles = arrayOfNulls<String>(app.applicationInfo.splitSourceDirs.size + 1)

                        for (i in app.applicationInfo.splitSourceDirs.indices) {
                            listOfSplitFiles[i] = app.applicationInfo.splitSourceDirs[i]
                            length += File(app.applicationInfo.splitSourceDirs[i]).length()
                        }

                        listOfSplitFiles[listOfSplitFiles.size - 1] = app.applicationInfo.sourceDir

                        for (file in listOfSplitFiles) {
                            contentResolverOutputStream = contentResolver.openOutputStream(documentFile!!.uri, "w")
                            bufferedOutputStream = BufferedOutputStream(contentResolverOutputStream)
                            zipOutputStream = ZipOutputStream(bufferedOutputStream)
                            fileInputStream = FileInputStream(file)
                            bufferedInputStream = BufferedInputStream(fileInputStream, buffer)

                            zipOutputStream!!.putNextEntry(ZipEntry(file?.substring(file.lastIndexOf("/") + 1)))

                            contentResolverOutputStream.use {
                                bufferedOutputStream.use {
                                    zipOutputStream.use {
                                        fileInputStream.use {
                                            bufferedInputStream.use { bufferedInputStream ->
                                                var count: Int
                                                while (bufferedInputStream!!.read(byteArray, 0, buffer).also { count = it } != -1) {
                                                    total += count
                                                    sendProgressBroadcast(total * 100 / length)
                                                    zipOutputStream!!.write(byteArray, 0, count)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else { // For APK files
                        sendApkTypeBroadcast(APK_TYPE_FILE)
                        val documentFile = pickedDir!!.createFile("application/vnd.android.package-archive", fileName)
                        val lengthOfFile = File(app.applicationInfo.sourceDir).length()

                        contentResolverOutputStream = contentResolver.openOutputStream(documentFile!!.uri, "w")
                        bufferedOutputStream = BufferedOutputStream(contentResolverOutputStream)
                        fileInputStream = FileInputStream(app.applicationInfo.sourceDir)

                        contentResolverOutputStream.use {
                            fileInputStream.use { fileInputStream ->
                                bufferedOutputStream.use { outputStream ->
                                    var len: Int
                                    while (fileInputStream!!.read(byteArray).also { len = it } > 0) {
                                        total += len
                                        sendProgressBroadcast(total * 100 / lengthOfFile)
                                        outputStream!!.write(byteArray, 0, len)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    /**
                     *  Terminate the process since the permission is
                     *  not granted, file cannot be copied
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
                        bufferedInputStream?.close()
                        fileInputStream?.close()
                        zipOutputStream?.close()
                        bufferedOutputStream?.close()
                        contentResolverOutputStream?.close()
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

    /* ----------------------------------------------------------------------------------------------------- */

    private fun sendApkTypeBroadcast(apkType: Int) {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionApkType, applicationContext, apkType)
    }

    private fun sendProgressBroadcast(apkType: Long) {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionApkType, applicationContext, apkType)
    }

    /* ----------------------------------------------------------------------------------------------------- */

    inner class BatchCopyBinder : Binder() {
        fun getService(): BatchCopyService {
            return this@BatchCopyService
        }
    }

    companion object {
        const val APK_TYPE_SPLIT = 1
        const val APK_TYPE_FILE = 2
    }
}