package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object ExtrasPreferences {

    const val highlight = "highlight_extensions_in_extras"
    const val extrasSearch = "extras_search"

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

}