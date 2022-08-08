package app.simple.inure.preferences

import androidx.dynamicanimation.animation.SpringForce

object BehaviourPreferences {

    private const val dimWindows = "is_dimming_windows_on"
    private const val coloredShadows = "are_colored_shadows_on"
    private const val transition = "is_transition_on"
    private const val arcAnimation = "is_animation_on"
    private const val marquee = "is_marquee_on"
    private const val skipLoading = "skip_main_loading_screen"

    const val stiffness = "scrolling_stiffness"
    const val dampingRatio = "scrolling_damping_ratio"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDimWindows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(dimWindows, boolean).apply()
    }

    fun isDimmingOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(dimWindows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColoredShadows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(coloredShadows, boolean).apply()
    }

    fun areColoredShadowsOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(coloredShadows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTransitionOn(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(transition, boolean).apply()
    }

    fun isTransitionOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(transition, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setArcAnimations(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(arcAnimation, boolean).apply()
    }

    fun isArcAnimationOn(): Boolean {
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

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDampingRatio(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(dampingRatio, value).apply()
    }

    fun getDampingRatio(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(dampingRatio, SpringForce.DAMPING_RATIO_NO_BOUNCY)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setStiffness(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(stiffness, value).apply()
    }

    fun getStiffness(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(stiffness, SpringForce.STIFFNESS_LOW)
    }
}