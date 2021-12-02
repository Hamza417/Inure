package app.simple.inure.preferences

import androidx.annotation.NonNull
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort
import org.jetbrains.annotations.NotNull

object SearchPreferences {

    private const val lastSearchKeyword = "last_search_keyword"
    const val sortStyle = "search_sort_style"
    const val isSortingReversed = "is_search_sorting_reversed"
    const val listAppsCategory = "search_list_apps_category"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(lastSearchKeyword, keyword).apply()
    }

    fun getLastSearchKeyword(): String {
        return getSharedPreferences().getString(lastSearchKeyword, "")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(@NonNull style: String) {
        getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setListAppCategory(@NonNull category: String) {
        getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getListAppCategory(): String {
        return getSharedPreferences().getString(listAppsCategory, AppCategoryPopup.BOTH)!!
    }
}
