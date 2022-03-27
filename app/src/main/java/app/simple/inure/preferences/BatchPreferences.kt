package app.simple.inure.preferences

object BatchPreferences {

    const val moveSelectionTop = "move_selection_on_top"
    const val highlightSelected = "highlight_selected_batch"

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
}