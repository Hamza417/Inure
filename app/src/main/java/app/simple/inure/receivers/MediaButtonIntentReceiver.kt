package app.simple.inure.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import app.simple.inure.BuildConfig
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.services.AudioService
import app.simple.inure.util.ParcelUtils.parcelable

/**
 * Used to control headset playback.
 * Single press: pause/resume
 * Double press: next track
 * Triple press: previous track
 */
class MediaButtonIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (DEBUG) Log.v(tag, "Received intent: $intent")
        if (handleIntent(context, intent) && isOrderedBroadcast) {
            abortBroadcast()
        }
    }

    companion object {
        private val DEBUG: Boolean = BuildConfig.DEBUG
        val tag: String = MediaButtonIntentReceiver::class.java.simpleName
        private const val MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2
        private const val DOUBLE_CLICK = 400
        private var mWakeLock: PowerManager.WakeLock? = null
        private var mClickCounter = 0
        private var mLastClickTime: Long = 0

        @SuppressLint("HandlerLeak") // false alarm, handler is already static
        private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT -> {
                        val clickCount: Int = msg.arg1
                        if (DEBUG) Log.v(tag, "Handling headset click, count = $clickCount")
                        val command: String? = when (clickCount) {
                            1 -> ServiceConstants.actionTogglePause
                            else -> null
                        }
                        if (command != null) {
                            val context: Context = msg.obj as Context
                            startService(context, command)
                        }
                    }
                }
                releaseWakeLockIfHandlerIdle()
            }
        }

        fun handleIntent(context: Context, intent: Intent): Boolean {
            val intentAction = intent.action
            if (Intent.ACTION_MEDIA_BUTTON == intentAction) {
                val event: KeyEvent = intent.parcelable(Intent.EXTRA_KEY_EVENT)
                    ?: return false
                val keycode: Int = event.keyCode
                val action: Int = event.action
                val eventTime = if (event.eventTime != 0L) event.eventTime else System.currentTimeMillis()
                // Fallback to system time if event time was not available.
                var command: String? = null
                when (keycode) {
                    KeyEvent.KEYCODE_MEDIA_STOP -> command = ServiceConstants.actionStop
                    KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> command = ServiceConstants.actionTogglePause
                    KeyEvent.KEYCODE_MEDIA_NEXT -> command = ServiceConstants.actionSkip
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> command = ServiceConstants.actionRewind
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> command = ServiceConstants.actionPause
                    KeyEvent.KEYCODE_MEDIA_PLAY -> command = ServiceConstants.actionPlay
                }
                if (command != null) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (event.repeatCount == 0) {
                            // Only consider the first event in a sequence, not the repeat events,
                            // so that we don't trigger in cases where the first event went to
                            // a different app (e.g. when the user ends a phone call by
                            // long pressing the headset button)

                            // The service may or may not be running, but we need to send it
                            // a command.
                            if (keycode == KeyEvent.KEYCODE_HEADSETHOOK || keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                                if (eventTime - mLastClickTime >= DOUBLE_CLICK) {
                                    mClickCounter = 0
                                }
                                mClickCounter++
                                if (DEBUG) Log.v(tag, "Got headset click, count = $mClickCounter")
                                mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)
                                val msg: Message = mHandler.obtainMessage(
                                        MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context)
                                val delay = if (mClickCounter < 3) DOUBLE_CLICK.toLong() else 0.toLong()
                                if (mClickCounter >= 3) {
                                    mClickCounter = 0
                                }
                                mLastClickTime = eventTime
                                acquireWakeLockAndSendMessage(context, msg, delay)
                            } else {
                                startService(context, command)
                            }
                            return true
                        }
                    }
                }
            }
            return false
        }

        private fun startService(context: Context, command: String) {
            val intent = Intent(context, AudioService::class.java)
            intent.action = command
            try {
                // IMPORTANT NOTE: (kind of a hack)
                // on Android O and above the following crashes when the app is not running
                // there is no good way to check whether the app is running so we catch the exception
                // we do not always want to use startForegroundService() because then one gets an ANR
                // if no notification is displayed via startForeground()
                // according to Play analytics this happens a lot, I suppose for example if command = PAUSE
                context.startService(intent)
            } catch (ignored: IllegalStateException) {
                ContextCompat.startForegroundService(context, intent)
            }
        }

        private fun acquireWakeLockAndSendMessage(context: Context, msg: Message, delay: Long) {
            if (mWakeLock == null) {
                val appContext: Context = context.applicationContext
                val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "felicit:headset_button")
                mWakeLock?.setReferenceCounted(false)
            }
            if (DEBUG) Log.v(tag, "Acquiring wake lock and sending " + msg.what)
            // Make sure we don't indefinitely hold the wake lock under any circumstances
            mWakeLock!!.acquire(10000)
            mHandler.sendMessageDelayed(msg, delay)
        }

        private fun releaseWakeLockIfHandlerIdle() {
            if (mHandler.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
                if (DEBUG) Log.v(tag, "Handler still has messages pending, not releasing wake lock")
                return
            }
            if (mWakeLock != null) {
                if (DEBUG) Log.v(tag, "Releasing wake lock")
                mWakeLock!!.release()
                mWakeLock = null
            }
        }
    }
}