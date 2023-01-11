package app.simple.inure.preferences

import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.util.SortBootManager

object BootManagerPreferences {

    const val sortingStyle = "boot_manager_sorting_style"
    const val sortingReversed = "boot_manager_sorting_reversed"
    const val appsCategory = "boot_manager_apps_category"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortingStyle(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sortingStyle, value).apply()
    }

    fun getSortingStyle(): String {
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
        return SharedPreferences.getSharedPreferences().getString(appsCategory, PopupAppsCategory.BOTH)!!
    }
}