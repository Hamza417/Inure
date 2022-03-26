package app.simple.inure.preferences

object BatchPreferences {

    const val moveSelectionTop = "move_selection_on_top"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMoveSelectionOnTop(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(moveSelectionTop, boolean).apply()
    }

    fun isSelectionOnTop(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(moveSelectionTop, false)
    }
}