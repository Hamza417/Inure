package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object ExtrasPreferences {

    const val highlight = "highlight_extensions_in_extras"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(highlight, value).apply()
    }

    fun isExtensionsHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(highlight, true)
    }

}