package app.simple.inure.preferences

import app.simple.inure.models.DevelopmentPreferencesModel

object DevelopmentPreferences {

    const val isWebViewXmlViewer = "is_xml_viewer_web_view"
    const val preferencesIndicator = "is_preferences_indicator_hidden"
    const val crashHandler = "is_using_native_crash_handler"
    const val music = "is_music_enabled"
    const val imageCaching = "is_image_caching_enabled"

    val developmentPreferences = listOf(
            DevelopmentPreferencesModel("Use WebView for XML Preview",
                                        "Use WebView for XML Preview instead of TextView.",
                                        isWebViewXmlViewer,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Hide Preferences Indicator",
                                        "Hides the indicators in the settings and dialogs.",
                                        preferencesIndicator,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Enable Music",
                                        "Enable music player in the app.",
                                        music,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Use Native Crash Handler",
                                        "Use native crash handler to catch crashes.",
                                        crashHandler,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN),

            DevelopmentPreferencesModel("Disable Image Caching",
                                        "Disable image caching to save memory but at the cost of higher CPU usage due to regeneration of all image data everytime they\'re loaded.",
                                        imageCaching,
                                        DevelopmentPreferencesModel.TYPE_BOOLEAN)
    )

    // ---------------------------------------------------------------------------------------------------------- //

    fun get(key: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(key, false)
    }

    fun set(key: String, value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(key, value).apply()
    }

    // ---------------------------------------------------------------------------------------------------------- //
}