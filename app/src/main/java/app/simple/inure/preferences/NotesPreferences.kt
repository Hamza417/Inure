package app.simple.inure.preferences

object NotesPreferences {

    const val expandedNotes = "expanded_notes"
    const val htmlSpans = "notes_editor_html_spans"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExpandedNotes(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(expandedNotes, boolean).apply()
    }

    fun areNotesExpanded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(expandedNotes, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHTMLSpans(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(htmlSpans, boolean).apply()
    }

    fun areHTMLSpans(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(htmlSpans, false)
    }
}