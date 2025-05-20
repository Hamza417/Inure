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

    // Audio Pager
    const val actionPreparedPager = "$appPackageName.preparedPager"
    const val actionTogglePausePager = "$appPackageName.toggle_pausePager"
    const val actionPlayPager = "$appPackageName.playPager"
    const val actionPausePager = "$appPackageName.pausePager"
    const val actionStopPager = "$appPackageName.stopPager"
    const val actionSkipPager = "$appPackageName.skipPager"
    const val actionRewindPager = "$appPackageName.rewindPager"
    const val actionQuitMusicServicePager = "$appPackageName.quit.music.servicePager"
    const val actionPendingQuitServicePager = "$appPackageName.pending_quit_servicePager"
    const val shuffleModePager = "$appPackageName.shuffle_modePage  r"
    const val actionNextPager = "$appPackageName.action_nextPager"
    const val actionPreviousPager = "$appPackageName.action_previousPager"
    const val actionOpenPager = "$appPackageName.action_openPager"
    const val actionMetaDataPager = "$appPackageName.metadataPager"
    const val actionBufferingPager = "$appPackageName.media.bufferingPager"
    const val actionMediaErrorPager = "$appPackageName.media.errorPager"

    // Batch
    const val actionBatchCopyStart = "$appPackageName.batch.copy.start"
    const val actionBatchCancel = "$appPackageName.action.cancel"
    const val actionBatchApkType = "$appPackageName.apk.type"
    const val actionExtractDone = "$appPackageName.extract.done"
    const val actionCopyProgress = "$appPackageName.copy.progress"
    const val actionCopyFinished = "$appPackageName.copy.completed"
    const val actionQuitExtractService = "$appPackageName.quit.extract.service"
    const val actionCopyProgressMax = "$appPackageName.max.progress.service"

    // VirusTotal
    const val ACTION_VIRUS_TOTAL_CANCEL = "$appPackageName.virustotal.cancel"

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