package app.simple.inure.preferences

import app.simple.inure.constants.Colors

object AccessibilityPreferences {

    const val IS_HIGHLIGHT_MODE = "is_highlight_mode"
    const val IS_HIGHLIGHT_STROKE = "is_highlight_stroke_enabled"
    const val BOTTOM_MENU_CONTEXT = "bottom_menu_context"
    const val COLORFUL_ICONS_PALETTE = "colorful_icons_palette"

    private const val IS_DIVIDER_ENABLED = "is_divider_enabled"
    private const val REDUCE_ANIMATIONS = "reduce_animations"
    private const val IS_COLORFUL_ICONS = "is_colorful_icons"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightMode(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_HIGHLIGHT_MODE, boolean).apply()
    }

    fun isHighlightMode(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_HIGHLIGHT_MODE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightStroke(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_HIGHLIGHT_STROKE, boolean).apply()
    }

    fun isHighlightStroke(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_HIGHLIGHT_STROKE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDivider(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_DIVIDER_ENABLED, boolean).apply()
    }

    fun isDividerEnabled(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_DIVIDER_ENABLED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReduceAnimations(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(REDUCE_ANIMATIONS, boolean).apply()
    }

    fun isAnimationReduced(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(REDUCE_ANIMATIONS, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppElementsContext(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(BOTTOM_MENU_CONTEXT, value).apply()
    }

    fun isAppElementsContext(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(BOTTOM_MENU_CONTEXT, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColorfulIcons(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_COLORFUL_ICONS, boolean).apply()
    }

    fun isColorfulIcons(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_COLORFUL_ICONS, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColorfulIconsPalette(palette: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(COLORFUL_ICONS_PALETTE, palette).apply()
    }

    fun getColorfulIconsPalette(): Int {
        return SharedPreferences.getSharedPreferences().getInt(COLORFUL_ICONS_PALETTE, Colors.PASTEL)
    }
}
