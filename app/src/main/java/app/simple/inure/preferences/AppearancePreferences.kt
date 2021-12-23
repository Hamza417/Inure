package app.simple.inure.preferences

import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.TypeFace

object AppearancePreferences {

    private const val appCornerRadius = "corner_radius"
    private const val iconShadows = "icon_shadows"
    private const val isHighlightMode = "is_highlight_mode"
    const val accentColor = "app_accent_color"
    const val appFont = "type_face"
    const val appTheme = "app_theme"
    const val accentOnNav = "accent_color_on_nav_bar"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColor(int: Int) {
        getSharedPreferences().edit().putInt(accentColor, int).apply()
    }

    fun getAccentColor(): Int {
        return getSharedPreferences().getInt(accentColor, 0)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppFont(@NonNull font: String) {
        getSharedPreferences().edit().putString(appFont, font).apply()
    }

    fun getAppFont(): String {
        return getSharedPreferences().getString(appFont, TypeFace.AUTO)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCornerRadius(@IntRange(from = 25, to = 400) radius: Int) {
        getSharedPreferences().edit().putInt(appCornerRadius, radius / 5).apply()
    }

    fun getCornerRadius(): Int {
        return getSharedPreferences().getInt(appCornerRadius, 60)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppTheme(@NonNull theme: Int) {
        getSharedPreferences().edit().putInt(appTheme, theme).apply()
    }

    fun getAppTheme(): Int {
        return getSharedPreferences().getInt(appTheme, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setIconShadows(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(iconShadows, boolean).apply()
    }

    fun isIconShadowsOn(): Boolean {
        return getSharedPreferences().getBoolean(iconShadows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentOnNavigationBar(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(accentOnNav, boolean).apply()
    }

    fun isAccentOnNavigationBar(): Boolean {
        return getSharedPreferences().getBoolean(accentOnNav, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHighlightMode(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(isHighlightMode, boolean).apply()
    }

    fun isHighlightMode(): Boolean {
        return getSharedPreferences().getBoolean(isHighlightMode, true)
    }
}