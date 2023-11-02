package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object BatchPreferences {

    const val moveSelectionsToTop = "move_selection_on_top"
    const val highlightSelected = "highlight_selected_batch"
    const val sortStyle = "batch_sort_style"
    const val isSortingReversed = "batch_is_sorting_reversed"
    const val listAppsCategory = "batch_list_apps_category"
    const val listAppsFilter = "batch_list_apps_filter"
    const val lastSelectedProfile = "batch_last_selected_profile"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMoveSelectionOnTop(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(moveSelectionsToTop, boolean).apply()
    }

    fun isSelectionOnTop(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(moveSelectionsToTop, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightSelectedBatch(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(highlightSelected, boolean).apply()
    }

    fun isSelectedBatchHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(highlightSelected, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(sortStyle, style).commit()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putBoolean(isSortingReversed, value).commit()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(category: String): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(listAppsCategory, category).commit()
    }

    fun getAppsCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(listAppsCategory, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(filter: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(listAppsFilter, filter).commit()
    }

    fun getAppsFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(listAppsFilter, SortConstant.ALL_BATCH_STATES)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastSelectedProfile(id: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(lastSelectedProfile, id).commit()
    }

    fun getLastSelectedProfile(): Int {
        return SharedPreferences.getSharedPreferences().getInt(lastSelectedProfile, -1)
    }
}