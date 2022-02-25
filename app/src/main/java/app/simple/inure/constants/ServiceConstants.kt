package app.simple.inure.constants

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
}