package app.simple.inure.preferences

object ExtrasPreferences {

    const val HIGHLIGHT = "highlight_extensions_in_extras"
    const val EXTRAS_SEARCH = "extras_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(HIGHLIGHT, value).apply()
    }

    fun isExtensionsHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(HIGHLIGHT, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(EXTRAS_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(EXTRAS_SEARCH, false)
    }
}
