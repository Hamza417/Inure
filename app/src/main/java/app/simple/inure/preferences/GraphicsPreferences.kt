package app.simple.inure.preferences

object GraphicsPreferences {

    const val EXTENSION_HIGHLIGHT = "highlight_extensions_in_graphics"
    const val GRAPHICS_SEARCH = "graphics_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(EXTENSION_HIGHLIGHT, value).apply()
    }

    fun isExtensionsHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(EXTENSION_HIGHLIGHT, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(GRAPHICS_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(GRAPHICS_SEARCH, false)
    }
}
