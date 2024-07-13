package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.popups.usagestats.PopupUsageStatsEngine
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortUsageStats
import app.simple.inure.util.UsageInterval

object StatisticsPreferences {

    const val APPS_CATEGORY = "stats_app_category"
    const val STATS_INTERVAL = "usage_stats_interval"
    const val STATS_SORTING = "stats_sorted_by"
    const val IS_SORTING_REVERSED = "stats_is_sorting_reversed"
    const val IS_UNUSED_HIDDEN = "are_unused_app_hidden"
    const val LIMIT_HOURS = "limits_stats_to_hours"
    const val STATS_ENGINE = "usage_stats_engine"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInterval(value: Int) {
        getSharedPreferences().edit().putInt(STATS_INTERVAL, value).apply()
    }

    fun getInterval(): @UsageInterval.IntervalType Int {
        return getSharedPreferences().getInt(STATS_INTERVAL, UsageInterval.WEEKlY)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortType(value: String) {
        getSharedPreferences().edit().putString(STATS_SORTING, value).apply()
    }

    fun getSortedBy(): String {
        return getSharedPreferences().getString(STATS_SORTING, SortUsageStats.TIME_USED)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setEngine(value: String) {
        getSharedPreferences().edit().putString(STATS_ENGINE, value).apply()
    }

    fun getEngine(): String {
        return getSharedPreferences().getString(STATS_ENGINE, PopupUsageStatsEngine.INURE)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_SORTING_REVERSED, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(IS_SORTING_REVERSED, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(value: String) {
        getSharedPreferences().edit().putString(APPS_CATEGORY, value).apply()
    }

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(APPS_CATEGORY, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUnusedAppState(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_UNUSED_HIDDEN, value).apply()
    }

    fun areUnusedAppHidden(): Boolean {
        return getSharedPreferences().getBoolean(IS_UNUSED_HIDDEN, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLimitToHours(value: Boolean) {
        getSharedPreferences().edit().putBoolean(LIMIT_HOURS, value).apply()
    }

    fun isLimitToHours(): Boolean {
        return getSharedPreferences().getBoolean(LIMIT_HOURS, false)
    }
}
