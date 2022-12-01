package app.simple.inure.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.simple.inure.constants.IntentConstants
import app.simple.inure.interfaces.receivers.AppUninstallCallbacks

class AppUninstalledBroadcastReceiver : BroadcastReceiver() {

    private var appUninstallCallbacks: AppUninstallCallbacks? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED
            || intent?.action == Intent.ACTION_PACKAGE_FULLY_REMOVED
            || intent?.action == IntentConstants.ACTION_APP_UNINSTALLED) {
            appUninstallCallbacks?.onAppUninstalledListener(intent.data?.schemeSpecificPart)
        }
    }

    fun setAppUninstallCallbacks(appUninstallCallbacks: AppUninstallCallbacks) {
        this.appUninstallCallbacks = appUninstallCallbacks
    }
}