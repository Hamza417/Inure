package app.simple.inure.preferences

object BehaviourPreferences {

    private const val dimWindows = "is_dimming_windows_on"
    private const val shadows = "are_shadows_on"
    private const val transition = "is_transition_on"
    private const val animations = "is_animation_on"

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
        SharedPreferences.getSharedPreferences().edit().putBoolean(animations, boolean).apply()
    }

    fun isAnimationOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(animations, true)
    }
}