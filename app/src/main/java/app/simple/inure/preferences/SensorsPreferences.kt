package app.simple.inure.preferences

import androidx.annotation.NonNull
import app.simple.inure.util.SortSensors
import org.jetbrains.annotations.NotNull

object SensorsPreferences {

    const val sortStyle = "sensor_sort_style"
    const val isSortingReversed = "is_sensor_sorting_reversed"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(@NonNull style: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(sortStyle, SortSensors.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isSortingReversed, false)
    }
}