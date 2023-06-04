package app.simple.inure.preferences

import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort

object SearchPreferences {

    private const val lastSearchKeyword = "last_search_keyword"
    const val sortStyle = "search_sort_style"
    const val isSortingReversed = "is_search_sorting_reversed"
    const val listAppsCategory = "search_list_apps_category"
    const val ignoreCasing = "search_ignore_case"
    const val deepSearch = "deep_search"
    const val deepSearchKeywordMode = "deep_search_keyword_mode"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(lastSearchKeyword, keyword).apply()
    }

    fun getLastSearchKeyword(): String {
        return getSharedPreferences().getString(lastSearchKeyword, "")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String) {
        getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(category: String) {
        getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(listAppsCategory, PopupAppsCategory.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setIgnoreCasing(value: Boolean) {
        getSharedPreferences().edit().putBoolean(ignoreCasing, value).apply()
    }

    fun isCasingIgnored(): Boolean {
        return getSharedPreferences().getBoolean(ignoreCasing, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDeepSearch(value: Boolean) {
        getSharedPreferences().edit().putBoolean(deepSearch, value).apply()
    }

    fun isDeepSearchEnabled(): Boolean {
        return getSharedPreferences().getBoolean(deepSearch, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchKeywordMode(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(deepSearchKeywordMode, value).commit()
    }

    fun isSearchKeywordModeEnabled(): Boolean {
        return getSharedPreferences().getBoolean(deepSearchKeywordMode, false)
    }
}
