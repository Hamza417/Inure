package app.simple.inure.preferences

object ConfigurationPreferences {

    private const val keepScreenOn = "keep_screen_on"
    const val isUsingRoot = "is_using_root"
    const val isUsingShizuku = "is_using_shizuku"
    const val language = "language_of_app"

    fun setKeepScreenOn(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(keepScreenOn, value).apply()
    }

    fun isKeepScreenOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(keepScreenOn, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingRoot(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isUsingRoot, value).apply()
    }

    fun isUsingRoot(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isUsingRoot, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppLanguage(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(language, value).apply()
    }

    fun getAppLanguage(): String? {
        return SharedPreferences.getSharedPreferences().getString(language, "default")
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingShizuku(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isUsingShizuku, value).apply()
    }

    fun isUsingShizuku(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isUsingShizuku, false)
    }
}