package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object OnBoardingPreferences {

    private const val isWelcomeDone = "is_welcome_done"
    private const val isTypefaceDone = "is_type_face_done"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setWelcomeState(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isWelcomeDone, value).apply()
    }

    fun isWelcomeDone(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isWelcomeDone, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTypeFaceState(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isTypefaceDone, value).apply()
    }

    fun isTypeFaceDone(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isTypefaceDone, false)
    }

}