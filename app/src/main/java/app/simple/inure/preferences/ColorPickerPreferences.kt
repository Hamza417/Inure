package app.simple.inure.preferences

object ColorPickerPreferences {

    const val COLOR_HUE_MODE = "color_hue_mode"

    private const val COLOR_HISTORY = "color_history"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColorHistory(color: String): Boolean {
        val history = getColorHistory()
        history.add(color)
        return SharedPreferences.getSharedPreferences().edit().putStringSet(COLOR_HISTORY, history).commit()
    }

    fun getColorHistory(): MutableSet<String> {
        return SharedPreferences.getSharedPreferences().getStringSet(COLOR_HISTORY, mutableSetOf())!!
    }
}
