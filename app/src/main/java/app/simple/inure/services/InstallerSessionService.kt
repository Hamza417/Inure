package app.simple.inure.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.constants.ServiceConstants

class InstallerSessionService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // InstallerUtils.setStatus(intent!!.getIntExtra(PackageInstaller.EXTRA_STATUS, -999), intent, applicationContext)
        println("InstallerSessionService action -> ${intent?.action}")
        intent.let {
            intent?.action = ServiceConstants.actionPackageInfo
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent!!)
        }

        stopSelf()
        return START_NOT_STICKY
    }
}