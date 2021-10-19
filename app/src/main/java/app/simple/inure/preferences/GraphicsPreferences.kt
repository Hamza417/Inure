package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object GraphicsPreferences {

    const val extensionHighlight = "highlight_extensions_in_graphics"
    const val graphicsSearch = "graphics_search"

    /** Extension Tags **/
    const val png = "png"
    const val jpg = "jpg"
    const val jpeg = "jpeg"
    const val gif = "gif"
    const val webp = "webp"
    const val svg = "svg"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(@NotNull value: Boolean) {
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

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilterVisibility(boolean: Boolean, extension: String) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(extension, boolean).apply()
    }

    fun isFilterAllowed(extension: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(extension, true)
    }

}