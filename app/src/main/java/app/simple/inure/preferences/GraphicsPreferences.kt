package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object GraphicsPreferences {

    const val extensionHighlight = "highlight_extensions_in_graphics"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightExtensions(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(extensionHighlight, value).apply()
    }

    fun isExtensionsHighlighted(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(extensionHighlight, true)
    }

}