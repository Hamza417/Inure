package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort

object SearchPreferences {

    const val SORT_STYLE = "search_sort_style"
    const val IS_SORTING_REVERSED = "is_search_sorting_reversed"
    const val APPS_CATEGORY = "search_list_apps_category"
    const val APPS_FILTER = "search_apps_filter"
    const val IGNORE_CASING = "search_ignore_case"
    const val DEEP_SEARCH = "deep_search"


    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String) {
        getSharedPreferences().edit().putString(SORT_STYLE, style).apply()
    }

    fun getSortStyle(): String {
        return getSharedPreferences().getString(SORT_STYLE, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_SORTING_REVERSED, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(IS_SORTING_REVERSED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(category: String) {
        getSharedPreferences().edit().putString(APPS_CATEGORY, category).apply()
    }

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(APPS_CATEGORY, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setIgnoreCasing(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IGNORE_CASING, value).apply()
    }

    fun isCasingIgnored(): Boolean {
        return getSharedPreferences().getBoolean(IGNORE_CASING, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDeepSearch(value: Boolean) {
        getSharedPreferences().edit().putBoolean(DEEP_SEARCH, value).apply()
    }

    fun isDeepSearchEnabled(): Boolean {
        return getSharedPreferences().getBoolean(DEEP_SEARCH, false)
    }


    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(filter: Int) {
        getSharedPreferences().edit().putInt(APPS_FILTER, filter).apply()
    }

    fun getAppsFilter(): Int {
        return getSharedPreferences().getInt(APPS_FILTER, SortConstant.ALL)
    }
}
