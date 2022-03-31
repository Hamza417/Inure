package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object DevelopmentPreferences {

    private const val isWebViewXmlViewer = "is_xml_viewer_web_view"
    const val isAudioPlayerFullScreen = "is_audio_player_full_screen"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setWebViewXmlViewer(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isWebViewXmlViewer, value).apply()
    }

    fun isWebViewXmlViewer(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isWebViewXmlViewer, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFullScreenAudioPlayer(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isAudioPlayerFullScreen, value).apply()
    }

    fun isAudioPlayerFullScreen(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isAudioPlayerFullScreen, false)
    }
}