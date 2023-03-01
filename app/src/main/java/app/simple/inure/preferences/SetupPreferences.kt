package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object SetupPreferences {

    private const val dontShowAgain = "dont_show_again"

    // ---------------------------------------------------------------------------------------------------------- //

    fun isDontShowAgain(): Boolean {
        return getSharedPreferences().getBoolean(dontShowAgain, false)
    }

    fun setDontShowAgain(value: Boolean) {
        getSharedPreferences().edit().putBoolean(dontShowAgain, value).apply()
    }
}