package app.simple.inure.preferences

object NotesPreferences {

    const val expandedNotes = "expanded_notes"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExpandedNotes(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(expandedNotes, boolean).apply()
    }

    fun areNotesExpanded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(expandedNotes, false)
    }
}