package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object BatteryOptimizationPreferences {

    const val batteryOptimizationCategory = "battery_optimization_category"
    const val batteryOptimizationSortStyle = "battery_optimization_sort_style"
    const val batteryOptimizationIsSortingReversed = "battery_optimization_is_sorting_reversed"
    const val batteryOptimizationFilter = "battery_optimization_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setApplicationType(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(batteryOptimizationCategory, value).apply()
    }

    fun getApplicationType(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(batteryOptimizationCategory,
                       SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(batteryOptimizationSortStyle, value).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(batteryOptimizationSortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortingReversed(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(batteryOptimizationIsSortingReversed, value).apply()
    }

    fun isSortingReversed(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(batteryOptimizationIsSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilter(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(batteryOptimizationFilter, value).apply()
    }

    fun getFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(batteryOptimizationFilter, SortConstant.ALL_OPTIMIZATION_STATES)
    }
}