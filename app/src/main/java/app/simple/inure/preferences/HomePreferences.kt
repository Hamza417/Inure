package app.simple.inure.preferences

import androidx.core.content.edit
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object HomePreferences {

    const val HOME_MENU_LAYOUT = "home_menu_layout"

    const val IS_TERMINAL_VISIBLE = "is_terminal_visible"
    const val IS_USAGE_STATISTICS_VISIBLE = "is_usage_statistics_visible"
    const val IS_ANALYTICS_VISIBLE = "is_analytics_visible"
    const val IS_MOST_USED_VISIBLE = "is_most_used_visible"
    const val IS_UNINSTALLED_VISIBLE = "is_uninstalled_visible"
    const val IS_DISABLED_VISIBLE = "is_disabled_visible"
    const val IS_STACKTRACES_VISIBLE = "is_stack_traces_visible"
    const val IS_BATTERY_OPTIMIZATION_VISIBLE = "is_battery_optimization_visible"
    const val IS_BOOT_MANAGER_VISIBLE = "is_boot_manager_visible"
    const val IS_SAVED_COMMANDS_VISIBLE = "is_saved_commands_visible"
    const val IS_APKS_VISIBLE = "is_apks_visible"
    const val IS_PREFERENCES_VISIBLE = "is_preferences_visible"
    const val IS_UNTAGGED_APPS_VISIBLE = "is_untagged_apps_visible"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMenuLayout(layout: Int) {
        getSharedPreferences().edit { putInt(HOME_MENU_LAYOUT, layout) }
    }

    fun getMenuLayout(): Int {
        return getSharedPreferences().getInt(HOME_MENU_LAYOUT, PopupMenuLayout.GRID)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setPanelVisibility(panel: String, isVisible: Boolean) {
        getSharedPreferences().edit { putBoolean(panel, isVisible) }
    }

    fun isPanelVisible(panel: String): Boolean {
        return getSharedPreferences().getBoolean(panel, true)
    }

    fun isUntaggedPanelVisible(): Boolean {
        return getSharedPreferences().getBoolean(IS_UNTAGGED_APPS_VISIBLE, false)
    }
}
