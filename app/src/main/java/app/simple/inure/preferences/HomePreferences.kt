package app.simple.inure.preferences

import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object HomePreferences {

    const val homeMenuLayout = "home_menu_layout"

    const val isTerminalVisible = "is_terminal_visible"
    const val isUsageStatisticsVisible = "is_usage_statistics_visible"
    const val isAnalyticsVisible = "is_analytics_visible"
    const val isMostUsedVisible = "is_most_used_visible"
    const val isUninstalledVisible = "is_uninstalled_visible"
    const val isDisabledVisible = "is_disabled_visible"
    const val isStackTracesVisible = "is_stack_traces_visible"
    const val isBatteryOptimizationVisible = "is_battery_optimization_visible"
    const val isBootManagerVisible = "is_boot_manager_visible"
    const val isSavedCommandsVisible = "is_saved_commands_visible"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMenuLayout(layout: Int) {
        getSharedPreferences().edit().putInt(homeMenuLayout, layout).apply()
    }

    fun getMenuLayout(): Int {
        return getSharedPreferences().getInt(homeMenuLayout, PopupMenuLayout.GRID)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setPanelVisibility(panel: String, isVisible: Boolean) {
        getSharedPreferences().edit().putBoolean(panel, isVisible).apply()
    }

    fun isPanelVisible(panel: String): Boolean {
        return getSharedPreferences().getBoolean(panel, true)
    }
}