package app.simple.inure.preferences

import androidx.annotation.NonNull
import org.jetbrains.annotations.NotNull

object ConfigurationPreferences {

    private const val keepScreenOn = "keep_screen_on"
    private const val showPermissionLabel = "is_permission_label_on"
    private const val isXmlViewerTextView = "is_xml_viewer_text_view"
    private const val sizeType = "size_type"
    private const val largeStrings = "load_large_strings"
    const val isUsingRoot = "is_using_root"

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
        return SharedPreferences.getSharedPreferences().getBoolean(isXmlViewerTextView, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingRoot(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isUsingRoot, value).apply()
    }

    fun isUsingRoot(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isUsingRoot, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSizeType(@NonNull font: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sizeType, font).apply()
    }

    fun getSizeType(): String {
        return SharedPreferences.getSharedPreferences().getString(sizeType, "si")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLoadLargeStrings(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(largeStrings, value).apply()
    }

    fun isLoadingLargeStrings(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(largeStrings, false)
    }
}