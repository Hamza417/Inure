package app.simple.inure.preferences

import androidx.annotation.NonNull
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.util.Sort
import org.jetbrains.annotations.NotNull

object BatchPreferences {

    const val moveSelectionTop = "move_selection_on_top"
    const val highlightSelected = "highlight_selected_batch"
    const val sortStyle = "batch_sort_style"
    const val isSortingReversed = "batch_is_sorting_reversed"
    const val listAppsCategory = "batch_list_apps_category"

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

    fun setSortStyle(@NonNull style: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(@NonNull category: String) {
        SharedPreferences.getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getAppsCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(listAppsCategory, PopupAppsCategory.BOTH)!!
    }
}