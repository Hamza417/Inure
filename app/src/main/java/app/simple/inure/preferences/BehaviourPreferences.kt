package app.simple.inure.preferences

import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupTransitionType

object BehaviourPreferences {

    private const val dimWindows = "is_dimming_windows_on"
    private const val blurWindow = "is_blurring_windows_on"
    private const val transition = "is_transition_on"
    private const val arcAnimation = "is_animation_on"
    private const val marquee = "is_marquee_on"
    private const val skipLoading = "skip_main_loading_screen"

    const val coloredShadows = "are_colored_shadows_on"
    const val transitionType = "panel_transition_type"
    const val arcType = "arc_type"
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

    fun setBlurWindows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(blurWindow, boolean).apply()
    }

    fun isBlurringOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(blurWindow, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColoredShadows(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(coloredShadows, boolean).apply()
    }

    fun isColoredShadow(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(coloredShadows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTransitionOn(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(transition, boolean).apply()
    }

    fun isTransitionOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(transition, true)
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

    fun setTransitionType(boolean: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(transitionType, boolean).apply()
    }

    fun getTransitionType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(transitionType, PopupTransitionType.SHARED_AXIS_Z)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setArcType(boolean: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(arcType, boolean).apply()
    }

    fun getArcType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(arcType, PopupArcType.INURE)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSkipLoadingMainScreenState(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(skipLoading, boolean).apply()
    }

    fun isSkipLoading(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(skipLoading, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDampingRatio(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(dampingRatio, value.coerceAtLeast(0.1F).coerceAtMost(1F)).apply()
    }

    fun getDampingRatio(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(dampingRatio, SpringForce.DAMPING_RATIO_NO_BOUNCY)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setStiffness(value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(stiffness, value).apply()
    }

    fun getStiffness(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(stiffness, SpringForce.STIFFNESS_LOW).coerceAtLeast(SpringForce.STIFFNESS_VERY_LOW).coerceAtMost(SpringForce.STIFFNESS_HIGH)
    }
}