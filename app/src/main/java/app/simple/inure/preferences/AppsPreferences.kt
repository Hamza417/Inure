package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object AppsPreferences {

    const val sortStyle = "sort_style"
    const val isSortingReversed = "is_sorting_reversed"
    const val appsType = "list_apps_type"
    const val appsFilter = "apps_filter_2"
    const val combineFilter = "combine_filter"
    const val appsCategory = "apps_category_flags"
    const val infoCustomFilter = "info_custom_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsType(category: String) {
        SharedPreferences.getSharedPreferences().edit().putString(appsType, category).apply()
    }

    fun getAppsType(): String {
        return SharedPreferences.getSharedPreferences().getString(appsType, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(value: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(appsFilter, value).commit()
    }

    fun getAppsFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(appsFilter, SortConstant.ALL)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCombineFilter(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(combineFilter, value).apply()
    }

    fun isCombineFilter(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(combineFilter, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(value: Long): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putLong(appsCategory, value).commit()
    }

    fun getAppsCategory(): Long {
        return SharedPreferences.getSharedPreferences().getLong(appsCategory, SortConstant.ALL_CATEGORIES)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setInfoCustomFilter(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(infoCustomFilter, value).apply()
    }

    fun getInfoCustomFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(infoCustomFilter, SortConstant.INFO_DEFAULT)
    }
}