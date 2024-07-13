package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object AppsPreferences {

    const val SORT_STYLE = "sort_style"
    const val IS_SORTING_REVERSED = "is_sorting_reversed"
    const val APPS_TYPE = "list_apps_type"
    const val APPS_FILTER = "apps_filter_2"
    const val COMBINE_FILTER = "combine_filter"
    const val APPS_CATEGORY = "apps_category_flags"
    const val INFO_CUSTOM_FILTER = "info_custom_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SORT_STYLE, style).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(SORT_STYLE, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_SORTING_REVERSED, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_SORTING_REVERSED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsType(category: String) {
        SharedPreferences.getSharedPreferences().edit().putString(APPS_TYPE, category).apply()
    }

    fun getAppsType(): String {
        return SharedPreferences.getSharedPreferences().getString(APPS_TYPE, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(value: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(APPS_FILTER, value).commit()
    }

    fun getAppsFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(APPS_FILTER, SortConstant.ALL)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCombineFilter(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(COMBINE_FILTER, value).apply()
    }

    fun isCombineFilter(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(COMBINE_FILTER, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(value: Long): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putLong(APPS_CATEGORY, value).commit()
    }

    fun getAppsCategory(): Long {
        return SharedPreferences.getSharedPreferences().getLong(APPS_CATEGORY, SortConstant.ALL_CATEGORIES)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInfoCustomFilter(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(INFO_CUSTOM_FILTER, value).apply()
    }

    fun getInfoCustomFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(INFO_CUSTOM_FILTER, SortConstant.INFO_DEFAULT)
    }
}
