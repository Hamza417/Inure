package app.simple.inure.preferences

object NotesPreferences {

    const val expandedNotes = "expanded_notes"
    const val jsonSpans = "notes_editor_json_spans"
    const val autoSave = "notes_editor_auto_save"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExpandedNotes(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(expandedNotes, boolean).apply()
    }

    fun areNotesExpanded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(expandedNotes, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setJSONSpans(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(jsonSpans, boolean).apply()
    }

    fun areJSONSpans(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(jsonSpans, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAutoSave(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(autoSave, boolean).apply()
    }

    fun isAutoSave(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(autoSave, false)
    }
}