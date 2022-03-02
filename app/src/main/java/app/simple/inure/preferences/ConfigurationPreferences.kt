package app.simple.inure.preferences

import org.jetbrains.annotations.NotNull

object ConfigurationPreferences {

    private const val keepScreenOn = "keep_screen_on"
    const val isUsingRoot = "is_using_root"

    fun setKeepScreenOn(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(keepScreenOn, value).apply()
    }

    fun isKeepScreenOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(keepScreenOn, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingRoot(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isUsingRoot, value).apply()
    }

    fun isUsingRoot(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isUsingRoot, false)
    }
}