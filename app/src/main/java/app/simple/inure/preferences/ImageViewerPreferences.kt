package app.simple.inure.preferences

object ImageViewerPreferences {

    const val IS_BACKGROUND_DARK = "is_image_viewer_background_dark"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setBackgroundMode(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_BACKGROUND_DARK, value).apply()
    }

    fun isBackgroundDark(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_BACKGROUND_DARK, true)
    }
}
