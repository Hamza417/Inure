package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.util.Sort

object BatchPreferences {

    const val MOVE_SELECTIONS_TO_TOP = "move_selection_on_top"
    const val HIGHLIGHT_SELECTED = "highlight_selected_batch"
    const val SORT_STYLE = "batch_sort_style"
    const val IS_SORTING_REVERSED = "batch_is_sorting_reversed"
    const val LIST_APPS_CATEGORY = "batch_list_apps_category"
    const val LIST_APPS_FILTER = "batch_list_apps_filter"
    const val LAST_SELECTED_PROFILE = "batch_last_selected_profile"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMoveSelectionOnTop(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(MOVE_SELECTIONS_TO_TOP, boolean).apply()
    }

    fun isSelectionOnTop(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(MOVE_SELECTIONS_TO_TOP, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightSelectedBatch(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(HIGHLIGHT_SELECTED, boolean).apply()
    }

    fun isSelectedBatchHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(HIGHLIGHT_SELECTED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(style: String): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(SORT_STYLE, style).commit()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(SORT_STYLE, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putBoolean(IS_SORTING_REVERSED, value).commit()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_SORTING_REVERSED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(category: String): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(LIST_APPS_CATEGORY, category).commit()
    }

    fun getAppsCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(LIST_APPS_CATEGORY, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsFilter(filter: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(LIST_APPS_FILTER, filter).commit()
    }

    fun getAppsFilter(): Int {
        return SharedPreferences.getSharedPreferences().getInt(LIST_APPS_FILTER, SortConstant.ALL_BATCH_STATES)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastSelectedProfile(id: Int): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putInt(LAST_SELECTED_PROFILE, id).commit()
    }

    fun getLastSelectedProfile(): Int {
        return SharedPreferences.getSharedPreferences().getInt(LAST_SELECTED_PROFILE, -1)
    }
}
