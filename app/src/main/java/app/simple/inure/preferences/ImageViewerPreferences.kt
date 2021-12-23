package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object ImageViewerPreferences {

    const val isBackgroundDark = "is_image_viewer_background_dark"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setBackgroundMode(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isBackgroundDark, value).apply()
    }

    fun isBackgroundDark(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isBackgroundDark, true)
    }
}