package app.simple.inure.preferences

object NotesPreferences {

    const val EXPANDED_NOTES = "expanded_notes"
    const val AUTO_SAVE = "notes_editor_auto_save"
    const val IS_GRID = "notes_is_grid"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExpandedNotes(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(EXPANDED_NOTES, boolean).apply()
    }

    fun areNotesExpanded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(EXPANDED_NOTES, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAutoSave(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(AUTO_SAVE, boolean).apply()
    }

    fun isAutoSave(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(AUTO_SAVE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGrid(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_GRID, boolean).apply()
    }

    fun getGrid(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_GRID, true)
    }
}
