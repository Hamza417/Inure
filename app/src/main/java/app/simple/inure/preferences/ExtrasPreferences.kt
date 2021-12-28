package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object ExtrasPreferences {

    const val highlight = "highlight_extensions_in_extras"
    const val extrasSearch = "extras_search"

    /* Extensions Tag */
    const val json = "json"
    const val html = "html"
    const val css = "css"
    const val properties = "properties"
    const val js = "js"
    const val tsv = "tsv"
    const val txt = "txt"
    const val proto = "proto"
    const val java = "java"
    const val bin = "bin"
    const val ttf = "ttf"
    const val md = "md"
    const val ini = "ini"
    const val version = "version"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(highlight, value).apply()
    }

    fun isExtensionsHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(highlight, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(extrasSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(extrasSearch, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFilterVisibility(boolean: Boolean, extension: String) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(extension, boolean).apply()
    }

    fun isFilterAllowed(extension: String): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(extension, true)
    }
}