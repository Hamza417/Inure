package app.simple.inure.constants

import android.media.MediaPlayer

object ServiceConstants {
    private const val appPackageName = "app.simple.inure"
    const val musicPackageName = "com.android.music"

    const val actionPrepared = "$appPackageName.prepared"
    const val actionTogglePause = "$appPackageName.toggle_pause"
    const val actionPlay = "$appPackageName.play"
    const val actionPause = "$appPackageName.pause"
    const val actionStop = "$appPackageName.stop"
    const val actionSkip = "$appPackageName.skip"
    const val actionRewind = "$appPackageName.rewind"
    const val actionQuitService = "$appPackageName.quit_service"
    const val actionPendingQuitService = "$appPackageName.pending_quit_service"
    const val shuffleMode = "$appPackageName.shuffle_mode"
    const val actionNext = "$appPackageName.action_next"
    const val actionPrevious = "$appPackageName.action_previous"
    const val actionOpen = "$appPackageName.action_open"
    const val actionMetaData = "$appPackageName.metadata"
    const val actionMediaError = "$appPackageName.media.error"

    fun getMediaErrorString(extra: Int): String {
        return when (extra) {
            MediaPlayer.MEDIA_ERROR_IO -> {
                "MEDIA_ERROR_IO"
            }
            MediaPlayer.MEDIA_ERROR_MALFORMED -> {
                "MEDIA_ERROR_MALFORMED"
            }
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> {
                "MEDIA_ERROR_UNSUPPORTED"
            }
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> {
                "MEDIA_ERROR_TIMED_OUT"
            }
            else -> {
                "NO_ERROR"
            }
        }
    }
}