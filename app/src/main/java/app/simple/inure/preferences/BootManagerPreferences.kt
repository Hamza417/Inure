package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.SortBootManager

object BootManagerPreferences {

    const val sortingStyle = "boot_manager_sorting_style"
    const val sortingReversed = "boot_manager_sorting_reversed"
    const val appsCategory = "boot_manager_apps_category"
    const val filter = "boot_manager_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sortingStyle, value).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(sortingStyle, SortBootManager.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortingReversed(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(sortingReversed, value).apply()
    }

    fun isSortingReversed(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(sortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(appsCategory, value).apply()
    }

    fun getAppsCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(appsCategory, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilter(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(filter, value).apply()
    }

    fun getFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(filter, SortConstant.ALL_BOOT_STATES)
    }
}