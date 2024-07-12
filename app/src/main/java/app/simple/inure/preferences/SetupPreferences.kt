package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object SetupPreferences {

    private const val DONT_SHOW_AGAIN = "dont_show_again"

    // ---------------------------------------------------------------------------------------------------------- //

    fun isDontShowAgain(): Boolean {
        return getSharedPreferences().getBoolean(DONT_SHOW_AGAIN, false)
    }

    fun setDontShowAgain(value: Boolean) {
        getSharedPreferences().edit().putBoolean(DONT_SHOW_AGAIN, value).apply()
    }
}
