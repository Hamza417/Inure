package app.simple.inure.preferences

object OnBoardingPreferences {

    private const val IS_WELCOME_DONE = "is_welcome_done"
    private const val IS_TYPEFACE_DONE = "is_type_face_done"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setWelcomeState(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_WELCOME_DONE, value).apply()
    }

    fun isWelcomeDone(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_WELCOME_DONE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setTypeFaceState(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_TYPEFACE_DONE, value).apply()
    }

    fun isTypeFaceDone(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_TYPEFACE_DONE, false)
    }

}
