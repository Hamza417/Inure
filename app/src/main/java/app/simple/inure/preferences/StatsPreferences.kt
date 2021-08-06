package app.simple.inure.preferences

import app.simple.inure.popups.usagestats.PopupAppsCategoryUsageStats
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortUsageStats
import app.simple.inure.util.UsageInterval
import org.jetbrains.annotations.NotNull

object StatsPreferences {
    const val appsCategory = "stats_app_category"
    const val statsInterval = "app_usage_intervals"
    const val statsSorting = "sorted_by"
    const val isSortingReversed = "stats_is_sorting_reversed"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInterval(value: String) {
        getSharedPreferences().edit().putString(statsInterval, value).apply()
    }

    fun getInterval(): String {
        return getSharedPreferences().getString(statsInterval, UsageInterval.WEEKlY)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortType(value: String) {
        getSharedPreferences().edit().putString(statsSorting, value).apply()
    }

    fun getSortedBy(): String {
        return getSharedPreferences().getString(statsSorting, SortUsageStats.TIME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(@NotNull value: Boolean) {
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
        return getSharedPreferences().getString(appsCategory, PopupAppsCategoryUsageStats.BOTH)!!
    }
}
