package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object BatteryOptimizationPreferences {

    const val BATTERY_OPTIMIZATION_CATEGORY = "battery_optimization_category"
    const val BATTERY_OPTIMIZATION_SORT_STYLE = "battery_optimization_sort_style"
    const val BATTERY_OPTIMIZATION_IS_SORTING_REVERSED = "battery_optimization_is_sorting_reversed"
    const val BATTERY_OPTIMIZATION_FILTER = "battery_optimization_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setApplicationType(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(BATTERY_OPTIMIZATION_CATEGORY, value).apply()
    }

    fun getApplicationType(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(BATTERY_OPTIMIZATION_CATEGORY,
                       SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(BATTERY_OPTIMIZATION_SORT_STYLE, value).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(BATTERY_OPTIMIZATION_SORT_STYLE, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortingReversed(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(BATTERY_OPTIMIZATION_IS_SORTING_REVERSED, value).apply()
    }

    fun isSortingReversed(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(BATTERY_OPTIMIZATION_IS_SORTING_REVERSED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilter(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(BATTERY_OPTIMIZATION_FILTER, value).apply()
    }

    fun getFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(BATTERY_OPTIMIZATION_FILTER, SortConstant.ALL_OPTIMIZATION_STATES)
    }
}
