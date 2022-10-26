package app.simple.inure.constants

import android.media.MediaPlayer

object ServiceConstants {

    private const val appPackageName = "app.simple.inure"

    // Audio
    const val actionPrepared = "$appPackageName.prepared"
    const val actionTogglePause = "$appPackageName.toggle_pause"
    const val actionPlay = "$appPackageName.play"
    const val actionPause = "$appPackageName.pause"
    const val actionStop = "$appPackageName.stop"
    const val actionSkip = "$appPackageName.skip"
    const val actionRewind = "$appPackageName.rewind"
    const val actionQuitMusicService = "$appPackageName.quit.music.service"
    const val actionPendingQuitService = "$appPackageName.pending_quit_service"
    const val shuffleMode = "$appPackageName.shuffle_mode"
    const val actionNext = "$appPackageName.action_next"
    const val actionPrevious = "$appPackageName.action_previous"
    const val actionOpen = "$appPackageName.action_open"
    const val actionMetaData = "$appPackageName.metadata"
    const val actionBuffering = "$appPackageName.media.buffering"
    const val actionMediaError = "$appPackageName.media.error"

    // Batch
    const val actionBatchCopyStart = "$appPackageName.batch.copy.start"
    const val actionBatchCancel = "$appPackageName.action.cancel"
    const val actionBatchApkType = "$appPackageName.apk.type"
    const val actionExtractDone = "$appPackageName.extract.done"
    const val actionCopyProgress = "$appPackageName.copy.progress"
    const val actionCopyFinished = "$appPackageName.copy.completed"
    const val actionQuitExtractService = "$appPackageName.quit.extract.service"
    const val actionCopyProgressMax = "$appPackageName.max.progress.service"

    // Installer
    const val actionPackageInfo = "$appPackageName.package.info"
    const val actionSessionStatus = "$appPackageName.session.status"

    // Widgets
    const val actionWidgetLockScreen = "$appPackageName.widget.lock_screen"
    const val actionDeviceAdminDisabled = "${appPackageName}.device_admin_action_disabled"
    const val actionDeviceAdminEnabled = "${appPackageName}.device_admin_action_enabled"

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