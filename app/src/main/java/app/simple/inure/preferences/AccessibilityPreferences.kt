package app.simple.inure.preferences

object AccessibilityPreferences {

    private const val isHighlightMode = "is_highlight_mode"
    private const val isDividerEnabled = "is_divider_enabled"
    private const val reduceAnimations = "reduce_animations"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightMode(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isHighlightMode, boolean).apply()
    }

    fun isHighlightMode(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isHighlightMode, false)
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
}