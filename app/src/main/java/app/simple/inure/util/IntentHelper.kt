package app.simple.inure.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object IntentHelper {

    const val INT_EXTRA = "int_extra"
    const val LONG_EXTRA = "long_extra"

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

    fun sendLocalBroadcastIntent(intentAction: String, context: Context, extra: Int) {
        Intent().also { intent ->
            intent.action = intentAction
            intent.putExtra(INT_EXTRA, extra)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    fun sendLocalBroadcastIntent(intentAction: String, context: Context, extra: Long) {
        Intent().also { intent ->
            intent.action = intentAction
            intent.putExtra(INT_EXTRA, extra)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    fun String?.asUri(): Uri? {
        return try {
            Uri.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    fun Uri?.openInBrowser(context: Context) {
        this ?: return // Do nothing if uri is null

        val browserIntent = Intent(Intent.ACTION_VIEW, this)
        browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, browserIntent, null)
    }
}