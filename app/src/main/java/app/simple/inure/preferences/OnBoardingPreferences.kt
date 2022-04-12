package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object OnBoardingPreferences {

    const val isWelcomeDone = "is_welcome_done"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setWelcomeState(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isWelcomeDone, value).apply()
    }

    fun isWelcomeDone(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isWelcomeDone, false)
    }

}