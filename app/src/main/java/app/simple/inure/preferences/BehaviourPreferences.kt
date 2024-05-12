package app.simple.inure.preferences

import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupTransitionType

object BehaviourPreferences {

    private const val DIM_WINDOWS = "is_dimming_windows_on"
    private const val BLUR_WINDOWS = "is_blurring_windows_on"
    private const val TRANSITION = "is_transition_on"
    private const val ARC_ANIMATION = "is_animation_on"
    private const val MARQUEE = "is_marquee_on"
    private const val SKIP_LOADING = "skip_main_loading_screen"

    const val COLORED_SHADOWS = "are_colored_shadows_on"
    const val TRANSITION_TYPE = "panel_transition_type"
    const val ARC_TYPE = "arc_type"
    const val STIFFNESS = "scrolling_stiffness"
    const val DAMPING_RATIO = "scrolling_damping_ratio"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDimWindows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(DIM_WINDOWS, boolean).apply()
    }

    fun isDimmingOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(DIM_WINDOWS, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setBlurWindows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(BLUR_WINDOWS, boolean).apply()
    }

    fun isBlurringOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(BLUR_WINDOWS, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColoredShadows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(COLORED_SHADOWS, boolean).apply()
    }

    fun isColoredShadow(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(COLORED_SHADOWS, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTransitionOn(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(TRANSITION, boolean).apply()
    }

    fun isTransitionOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(TRANSITION, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setArcAnimations(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(ARC_ANIMATION, boolean).apply()
    }

    fun isArcAnimationOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(ARC_ANIMATION, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMarquee(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(MARQUEE, boolean).apply()
    }

    fun isMarqueeOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(MARQUEE, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTransitionType(boolean: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(TRANSITION_TYPE, boolean).apply()
    }

    fun getTransitionType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(TRANSITION_TYPE, PopupTransitionType.SHARED_AXIS_Z)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setArcType(boolean: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(ARC_TYPE, boolean).apply()
    }

    fun getArcType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(ARC_TYPE, PopupArcType.INURE)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSkipLoadingMainScreenState(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(SKIP_LOADING, boolean).apply()
    }

    fun isSkipLoading(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(SKIP_LOADING, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDampingRatio(value: Float) {
        SharedPreferences.getSharedPreferences().edit()
            .putFloat(DAMPING_RATIO,
                      value.coerceAtLeast(0.1F).coerceAtMost(1F)).apply()
    }

    fun getDampingRatio(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(DAMPING_RATIO, SpringForce.DAMPING_RATIO_NO_BOUNCY)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setStiffness(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(STIFFNESS, value).apply()
    }

    fun getStiffness(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(STIFFNESS, SpringForce.STIFFNESS_LOW)
            .coerceAtLeast(SpringForce.STIFFNESS_VERY_LOW)
            .coerceAtMost(SpringForce.STIFFNESS_HIGH)
    }
}
