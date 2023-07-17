package app.simple.inure.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.simple.inure.constants.IntentConstants
import app.simple.inure.interfaces.receivers.AppUninstallCallbacks

class AppUninstalledBroadcastReceiver : BroadcastReceiver() {

    private var appUninstallCallbacks: AppUninstallCallbacks? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AppUninstalled", "onReceive: ${intent?.action}")

        when (intent?.action) {
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_FULLY_REMOVED,
            IntentConstants.ACTION_APP_UNINSTALLED -> {
                appUninstallCallbacks?.onAppUninstalled(intent.data?.schemeSpecificPart)
                Log.d("AppUninstalled", "onReceive: app removed ${intent.data?.schemeSpecificPart}")
            }

            Intent.ACTION_PACKAGE_ADDED -> {
                Log.d("AppUninstalled", "onReceive: app added ${intent.data?.schemeSpecificPart}")
                appUninstallCallbacks?.onAppInstalled(intent.data?.schemeSpecificPart)
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d("AppUninstalled", "onReceive: New app added ${intent.data?.schemeSpecificPart}")
                appUninstallCallbacks?.onAppReplaced(intent.data?.schemeSpecificPart)
            }
        }
    }

    fun setAppUninstallCallbacks(appUninstallCallbacks: AppUninstallCallbacks) {
        this.appUninstallCallbacks = appUninstallCallbacks
    }
}