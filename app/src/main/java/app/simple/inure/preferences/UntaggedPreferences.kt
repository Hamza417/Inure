package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object UntaggedPreferences {

    const val SORT_STYLE = "untagged_sort_style"
    const val IS_SORTING_REVERSED = "untagged_is_sorting_reversed"
    const val APPS_TYPE = "untagged_apps_type"

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
}
