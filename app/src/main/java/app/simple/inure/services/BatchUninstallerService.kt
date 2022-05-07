package app.simple.inure.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.IBinder

class BatchUninstallerService : Service() {

    private val binder = BatchUninstallerBinder()

    var list = arrayListOf<PackageInfo>()
        set(value) {
            field = value
        }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

    }

    /* ----------------------------------------------------------------------------------------------------- */

    fun setAppsListForUninstallation(list: ArrayList<PackageInfo>) {

    }

    /* ----------------------------------------------------------------------------------------------------- */

    inner class BatchUninstallerBinder : Binder() {
        fun getService(): BatchUninstallerService {
            return this@BatchUninstallerService
        }
    }
}