package app.simple.inure.preferences

import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.popups.usagestats.PopupUsageStatsEngine
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortUsageStats
import app.simple.inure.util.UsageInterval

object StatisticsPreferences {

    const val appsCategory = "stats_app_category"
    const val statsInterval = "usage_stats_interval"
    const val statsSorting = "stats_sorted_by"
    const val isSortingReversed = "stats_is_sorting_reversed"
    const val isUnusedHidden = "are_unused_app_hidden"
    const val limitHours = "limits_stats_to_hours"
    const val statsEngine = "usage_stats_engine"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInterval(value: Int) {
        getSharedPreferences().edit().putInt(statsInterval, value).apply()
    }

    fun getInterval(): @UsageInterval.IntervalType Int {
        return getSharedPreferences().getInt(statsInterval, UsageInterval.WEEKlY)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortType(value: String) {
        getSharedPreferences().edit().putString(statsSorting, value).apply()
    }

    fun getSortedBy(): String {
        return getSharedPreferences().getString(statsSorting, SortUsageStats.TIME_USED)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setEngine(value: String) {
        getSharedPreferences().edit().putString(statsEngine, value).apply()
    }

    fun getEngine(): String {
        return getSharedPreferences().getString(statsEngine, PopupUsageStatsEngine.INURE)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(value: String) {
        getSharedPreferences().edit().putString(appsCategory, value).apply()
    }

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(appsCategory, PopupAppsCategory.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUnusedAppState(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isUnusedHidden, value).apply()
    }

    fun areUnusedAppHidden(): Boolean {
        return getSharedPreferences().getBoolean(isUnusedHidden, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLimitToHours(value: Boolean) {
        getSharedPreferences().edit().putBoolean(limitHours, value).apply()
    }

    fun isLimitToHours(): Boolean {
        return getSharedPreferences().getBoolean(limitHours, false)
    }
}
