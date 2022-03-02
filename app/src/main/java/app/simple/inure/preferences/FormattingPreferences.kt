package app.simple.inure.preferences

import androidx.annotation.NonNull
import org.jetbrains.annotations.NotNull

object FormattingPreferences {

    private const val isXmlViewerTextView = "is_xml_viewer_text_view"
    private const val sizeType = "size_type"
    private const val largeStrings = "load_large_strings"
    private const val dateFormat = "app_date_format"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setXmlViewerTextView(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isXmlViewerTextView, value).apply()
    }

    fun isXmlViewerTextView(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isXmlViewerTextView, true)
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

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDateFormat(@NonNull font: String) {
        SharedPreferences.getSharedPreferences().edit().putString(dateFormat, font).apply()
    }

    fun getDateFormat(): String {
        return SharedPreferences.getSharedPreferences().getString(dateFormat, "EEE, yyyy MMM dd, hh:mm a")!!
    }
}