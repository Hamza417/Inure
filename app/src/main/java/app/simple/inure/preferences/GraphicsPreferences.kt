package app.simple.inure.preferences

object GraphicsPreferences {

    const val extensionHighlight = "highlight_extensions_in_graphics"
    const val graphicsSearch = "graphics_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(extensionHighlight, value).apply()
    }

    fun isExtensionsHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(extensionHighlight, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(graphicsSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(graphicsSearch, false)
    }
}