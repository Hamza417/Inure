package app.simple.inure.preferences

object UsageMonitorPreferences {

    const val USAGE_MONITOR_EXCLUDE_SYSTEM = "usage_monitor_exclude_system"
    const val USAGE_MONITOR_REFRESH_INTERVAL = "usage_monitor_refresh_interval"
    const val USAGE_MONITOR_FILTER_PERMISSION = "usage_monitor_filter_permission"
    const val USAGE_MONITOR_SHOW_INACTIVE = "usage_monitor_show_inactive"
    const val USAGE_MONITOR_SEARCH = "usage_monitor_search"
    const val USAGE_MONITOR_TIME_THRESHOLD = "usage_monitor_time_threshold"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExcludeSystemApps(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(USAGE_MONITOR_EXCLUDE_SYSTEM, boolean).apply()
    }

    fun isExcludingSystemApps(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(USAGE_MONITOR_EXCLUDE_SYSTEM, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setRefreshInterval(interval: Long) {
        SharedPreferences.getSharedPreferences().edit().putLong(USAGE_MONITOR_REFRESH_INTERVAL, interval).apply()
    }

    fun getRefreshInterval(): Long {
        return SharedPreferences.getSharedPreferences().getLong(USAGE_MONITOR_REFRESH_INTERVAL, 5000L) // 5 seconds default
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilterPermission(permission: String?) {
        SharedPreferences.getSharedPreferences().edit().putString(USAGE_MONITOR_FILTER_PERMISSION, permission).apply()
    }

    fun getFilterPermission(): String? {
        return SharedPreferences.getSharedPreferences().getString(USAGE_MONITOR_FILTER_PERMISSION, null)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShowInactive(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(USAGE_MONITOR_SHOW_INACTIVE, boolean).apply()
    }

    fun isShowingInactive(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(USAGE_MONITOR_SHOW_INACTIVE, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(USAGE_MONITOR_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(USAGE_MONITOR_SEARCH, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTimeThreshold(threshold: Long) {
        SharedPreferences.getSharedPreferences().edit().putLong(USAGE_MONITOR_TIME_THRESHOLD, threshold).apply()
    }

    fun getTimeThreshold(): Long {
        return SharedPreferences.getSharedPreferences().getLong(USAGE_MONITOR_TIME_THRESHOLD, 5 * 60 * 1000L) // 5 minutes default
    }
}
