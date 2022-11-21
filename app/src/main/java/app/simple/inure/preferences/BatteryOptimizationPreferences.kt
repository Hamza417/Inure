package app.simple.inure.preferences

import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.util.Sort

object BatteryOptimizationPreferences {

    const val batteryOptimizationCategory = "battery_optimization_category"
    const val batteryOptimizationSortStyle = "battery_optimization_sort_style"
    const val batteryOptimizationIsSortingReversed = "battery_optimization_is_sorting_reversed"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setBatteryOptimizationCategory(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(batteryOptimizationCategory, value).apply()
    }

    fun getBatteryOptimizationCategory(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(batteryOptimizationCategory,
                       PopupAppsCategory.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setBatteryOptimizationSortStyle(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(batteryOptimizationSortStyle, value).apply()
    }

    fun getBatteryOptimizationSortStyle(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(batteryOptimizationSortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setBatteryOptimizationIsSortingReversed(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(batteryOptimizationIsSortingReversed, value).apply()
    }

    fun isBatteryOptimizationSortingReversed(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(batteryOptimizationIsSortingReversed, false)
    }
}