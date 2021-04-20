package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object ConfigurationPreferences {

    private const val keepScreenOn = "keep_screen_on"
    private const val showPermissionLabel = "is_permission_label_on"
    private const val isXmlViewerTextView = "is_xml_viewer_text_view"

    fun setKeepScreenOn(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(keepScreenOn, value).apply()
    }

    fun isKeepScreenOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(keepScreenOn, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setPermissionLabelMode(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(showPermissionLabel, value).apply()
    }

    fun getPermissionLabelMode(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(showPermissionLabel, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setXmlViewerTextView(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isXmlViewerTextView, value).apply()
    }

    fun isXmlViewerTextView(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isXmlViewerTextView, false)
    }
}