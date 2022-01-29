package app.simple.inure.preferences

object BehaviourPreferences {

    private const val dimWindows = "is_dimming_windows_on"
    private const val shadows = "are_shadows_on"
    private const val transition = "is_transition_on"
    private const val arcAnimation = "is_animation_on"
    private const val marquee = "is_marquee_on"
    private const val skipLoading = "skip_main_loading_screen"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDimWindows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(dimWindows, boolean).apply()
    }

    fun isDimmingOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(dimWindows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShadows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(shadows, boolean).apply()
    }

    fun areShadowsOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(shadows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTransitionOn(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(transition, boolean).apply()
    }

    fun isTransitionOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(transition, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAnimations(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(arcAnimation, boolean).apply()
    }

    fun isAnimationOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(arcAnimation, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMarquee(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(marquee, boolean).apply()
    }

    fun isMarqueeOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(marquee, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSkipLoadingMainScreenState(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(skipLoading, boolean).apply()
    }

    fun isSkipLoadingMainScreenState(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(skipLoading, false)
    }
}