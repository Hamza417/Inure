package app.simple.inure.preferences

import app.simple.inure.util.SortSensors

object SensorsPreferences {

    const val SORT_STYLE = "sensor_sort_style"
    const val IS_SORTING_REVERSED = "is_sensor_sorting_reversed"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SORT_STYLE, style).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(SORT_STYLE, SortSensors.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_SORTING_REVERSED, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_SORTING_REVERSED, false)
    }
}
