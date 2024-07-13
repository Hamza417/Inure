package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.SortBootManager

object BootManagerPreferences {

    const val SORTING_STYLE = "boot_manager_sorting_style"
    const val SORTING_REVERSED = "boot_manager_sorting_reversed"
    const val APPS_CATEGORY = "boot_manager_apps_category"
    const val FILTER = "boot_manager_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SORTING_STYLE, value).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(SORTING_STYLE, SortBootManager.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortingReversed(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(SORTING_REVERSED, value).apply()
    }

    fun isSortingReversed(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(SORTING_REVERSED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(APPS_CATEGORY, value).apply()
    }

    fun getAppsCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(APPS_CATEGORY, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilter(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(FILTER, value).apply()
    }

    fun getFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(FILTER, SortConstant.ALL_BOOT_STATES)
    }
}
