package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort

object SearchPreferences {

    private const val LAST_SEARCH_KEYWORD = "last_search_keyword"
    const val SORT_STYLE = "search_sort_style"
    const val IS_SORTING_REVERSED = "is_search_sorting_reversed"
    const val APPS_CATEGORY = "search_list_apps_category"
    const val APPS_FILTER = "search_apps_filter"
    const val IGNORE_CASING = "search_ignore_case"
    const val DEEP_SEARCH = "deep_search"

    private const val DEEP_SEARCH_KEYWORD_MODE = "deep_search_keyword_mode"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(LAST_SEARCH_KEYWORD, keyword).apply()
    }

    fun getLastSearchKeyword(): String {
        return getSharedPreferences().getString(LAST_SEARCH_KEYWORD, "")!!
    }

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

    fun setSearchKeywordMode(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(DEEP_SEARCH_KEYWORD_MODE, value).commit()
    }

    fun isSearchKeywordModeEnabled(): Boolean {
        return getSharedPreferences().getBoolean(DEEP_SEARCH_KEYWORD_MODE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(filter: Int) {
        getSharedPreferences().edit().putInt(APPS_FILTER, filter).apply()
    }

    fun getAppsFilter(): Int {
        return getSharedPreferences().getInt(APPS_FILTER, SortConstant.ALL)
    }
}
