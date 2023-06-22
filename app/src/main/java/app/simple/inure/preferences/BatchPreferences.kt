package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object BatchPreferences {

    const val moveSelectionTop = "move_selection_on_top"
    const val highlightSelected = "highlight_selected_batch"
    const val sortStyle = "batch_sort_style"
    const val isSortingReversed = "batch_is_sorting_reversed"
    const val listAppsCategory = "batch_list_apps_category"
    const val listAppsFilter = "batch_list_apps_filter"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMoveSelectionOnTop(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(moveSelectionTop, boolean).apply()
    }

    fun isSelectionOnTop(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(moveSelectionTop, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightSelectedBatch(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(highlightSelected, boolean).apply()
    }

    fun isSelectedBatchHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(highlightSelected, false)
    }

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

    fun setAppsCategory(category: String) {
        SharedPreferences.getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getAppsCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(listAppsCategory, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(filter: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(listAppsFilter, filter).apply()
    }

    fun getAppsFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(listAppsFilter, SortConstant.ALL_BATCH_STATES)
    }
}