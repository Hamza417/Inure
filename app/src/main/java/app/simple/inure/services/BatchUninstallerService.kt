package app.simple.inure.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.IBinder

class BatchUninstallerService : Service() {

    private val binder = BatchUninstallerBinder()
    private var list = arrayListOf<PackageInfo>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /* ----------------------------------------------------------------------------------------------------- */

    fun setAppsListForUninstallation(list: ArrayList<PackageInfo>) {
        kotlin.runCatching {

        }
    }

    /* ----------------------------------------------------------------------------------------------------- */

    inner class BatchUninstallerBinder : Binder() {
        fun getService(): BatchUninstallerService {
            return this@BatchUninstallerService
        }
    }
}