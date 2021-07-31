package app.simple.inure.preferences

import android.app.usage.UsageStatsManager
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortUsageStats
import org.jetbrains.annotations.NotNull

object StatsPreferences {
    const val statsInterval = "app_usage_intervals"
    const val statsSorting = "sorted_by"
    const val isSortingReversed = "stats_is_sorting_reversed"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInterval(int: Int) {
        getSharedPreferences().edit().putInt(statsInterval, int).apply()
    }

    fun getInterval(): Int {
        return getSharedPreferences().getInt(statsInterval, UsageStatsManager.INTERVAL_MONTHLY)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortType(value: String) {
        getSharedPreferences().edit().putString(statsSorting, value).apply()
    }

    fun getSortedBy(): String {
        return getSharedPreferences().getString(statsSorting, SortUsageStats.DATA_RECEIVED)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, false)
    }
}
