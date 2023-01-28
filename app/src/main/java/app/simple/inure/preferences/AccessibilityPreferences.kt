package app.simple.inure.preferences

object AccessibilityPreferences {

    private const val isHighlightMode = "is_highlight_mode"
    private const val isHighlightStroke = "is_highlight_stroke_enabled"
    private const val isDividerEnabled = "is_divider_enabled"
    private const val reduceAnimations = "reduce_animations"
    private const val bottomMenuContext = "bottom_menu_context"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightMode(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isHighlightMode, boolean).apply()
    }

    fun isHighlightMode(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isHighlightMode, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightStroke(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isHighlightStroke, boolean).apply()
    }

    fun isHighlightStroke(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isHighlightStroke, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDivider(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isDividerEnabled, boolean).apply()
    }

    fun isDividerEnabled(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isDividerEnabled, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReduceAnimations(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(reduceAnimations, boolean).apply()
    }

    fun isAnimationReduced(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(reduceAnimations, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppElementsContext(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(bottomMenuContext, value).apply()
    }

    fun isAppElementsContext(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(bottomMenuContext, false)
    }
}