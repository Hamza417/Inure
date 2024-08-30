package app.simple.inure.preferences

object ColorPickerPreferences {

    const val COLOR_HUE_MODE = "color_hue_mode"

    // ---------------------------------------------------------------------------------------------------------- //

    const val COLOR_HUE_MODE_DEFAULT = 0
    const val COLOR_HUE_MODE_PASTEL = 1

    fun setColorHueMode(mode: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(COLOR_HUE_MODE, mode).commit()
    }

    fun getColorHueMode(): Int {
        return SharedPreferences.getSharedPreferences().getInt(COLOR_HUE_MODE, COLOR_HUE_MODE_DEFAULT)
    }

    fun isColorHueModePastel(): Boolean {
        return getColorHueMode() == COLOR_HUE_MODE_PASTEL
    }

    fun isColorHueModeDefault(): Boolean {
        return getColorHueMode() == COLOR_HUE_MODE_DEFAULT
    }
}
