package app.simple.inure.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.util.FileUtils

class InstallerService : Service() {

    private val installerThread = Thread(InstallerRunnable())

    private val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNING_CERTIFICATES or
                PackageManager.GET_SHARED_LIBRARY_FILES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_META_DATA or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SIGNATURES or
                PackageManager.GET_SHARED_LIBRARY_FILES
    }

    private var packageInfo: PackageInfo? = null

    var uri: Uri? = null
        set(value) {
            if (field != value) {
                field = value
                installerThread.start()
            }
        }

    override fun onBind(intent: Intent?): IBinder {
        return InstallerServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    inner class InstallerServiceBinder : Binder() {
        fun getService(): InstallerService {
            return this@InstallerService
        }
    }

    inner class InstallerRunnable : Runnable {
        override fun run() {
            prepareInstallation()
        }
    }

    private fun prepareInstallation() {
        PackageData.makePackageFolder(applicationContext)

        uri!!.let { it ->
            val name = DocumentFile.fromSingleUri(applicationContext, it)?.name
            val file = applicationContext.getInstallerDir(name!!)

            contentResolver.openInputStream(it).use {
                FileUtils.copyStreamToFile(it!!, file)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageInfo = packageManager.getPackageArchiveInfo(file.path, PackageManager.PackageInfoFlags.of(flags.toLong()))!!
            } else {
                @Suppress("DEPRECATION")
                packageInfo = packageManager.getPackageArchiveInfo(file.path, flags)
            }

            Intent().let {
                it.action = ServiceConstants.actionPackageInfo
                it.putExtra(BundleConstants.packageInfo, packageInfo)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kotlin.runCatching {
            installerThread.interrupt()
        }
    }
}