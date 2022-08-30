package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object DevelopmentPreferences {

    private const val isWebViewXmlViewer = "is_xml_viewer_web_view"
    private const val preferencesIndicator = "is_preferences_indicator_hidden"
    private const val debugFeatures = "is_debug_features_enabled"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setWebViewXmlViewer(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isWebViewXmlViewer, value).apply()
    }

    fun isWebViewXmlViewer(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isWebViewXmlViewer, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHidePreferencesIndicator(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(preferencesIndicator, value).apply()
    }

    fun isPreferencesIndicatorHidden(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(preferencesIndicator, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDebugFeaturesState(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(debugFeatures, value).apply()
    }

    fun isDebugStateEnabled(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(debugFeatures, false)
    }
}