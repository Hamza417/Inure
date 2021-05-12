package app.simple.inure.preferences

import android.app.usage.UsageStatsManager
import app.simple.inure.viewmodels.UsageStatsData

object StatsPreferences {
    const val statsInterval = "app_usage_intervals"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInterval(int: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(statsInterval, int).apply()
    }

    fun getInterval(): Int {
        return SharedPreferences.getSharedPreferences().getInt(statsInterval, UsageStatsManager.INTERVAL_WEEKLY)
    }
}