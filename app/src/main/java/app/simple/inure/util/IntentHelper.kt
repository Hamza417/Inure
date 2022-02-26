package app.simple.inure.util

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object IntentHelper {
    /**
     * Quickly broadcast a application level local intent
     * Make sure the intent is registered in the receiving context
     *
     * @param intentAction is the action
     * @param context
     */
    fun sendLocalBroadcastIntent(intentAction: String, context: Context) {
        Intent().also { intent ->
            intent.action = intentAction
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    fun sendLocalBroadcastIntent(intentAction: String, context: Context, extra: String) {
        Intent().also { intent ->
            intent.action = intentAction
            intent.putExtra("stringExtra", extra)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}