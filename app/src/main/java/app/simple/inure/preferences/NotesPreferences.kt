package app.simple.inure.preferences

object NotesPreferences {

    const val expandedNotes = "expanded_notes"
    const val autoSave = "notes_editor_auto_save"
    const val isGrid = "notes_is_grid"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExpandedNotes(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(expandedNotes, boolean).apply()
    }

    fun areNotesExpanded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(expandedNotes, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAutoSave(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(autoSave, boolean).apply()
    }

    fun isAutoSave(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(autoSave, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGrid(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isGrid, boolean).apply()
    }

    fun getGrid(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isGrid, true)
    }
}