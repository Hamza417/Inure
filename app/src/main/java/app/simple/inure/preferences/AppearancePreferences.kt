package app.simple.inure.preferences

import androidx.annotation.IntRange
import androidx.annotation.NonNull
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.TypeFace

object AppearancePreferences {

    private const val appCornerRadius = "corner_radius"
    private const val iconShadows = "icon_shadows"
    private const val lastDarkTheme = "last_dark_theme"
    private const val coloredIconShadows = "icon_shadows_colored"

    const val theme = "current_app_theme"
    const val accentColor = "app_accent_color"
    const val appFont = "type_face"
    const val accentOnNav = "accent_color_on_nav_bar"
    const val transparentStatus = "is_transparent_status_disabled"
    const val iconSize = "app_icon_size"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColor(int: Int) {
        getSharedPreferences().edit().putInt(accentColor, int).apply()
    }

    fun getAccentColor(): Int {
        return getSharedPreferences().getInt(accentColor, 0)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    /**
     * @param value for storing theme preferences
     * 0 - Light
     * 1 - Dark
     * 2 - AMOLED
     * 3 - System
     * 4 - Day/Night
     */
    fun setTheme(value: Int) {
        getSharedPreferences().edit().putInt(theme, value).apply()
    }

    fun getTheme(): Int {
        return getSharedPreferences().getInt(theme, ThemeConstants.FOLLOW_SYSTEM)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastDarkTheme(value: Int) {
        getSharedPreferences().edit().putInt(lastDarkTheme, value).apply()
    }

    fun getLastDarkTheme(): Int {
        return getSharedPreferences().getInt(lastDarkTheme, ThemeConstants.DARK_THEME)
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

    fun setTransparentStatusState(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(transparentStatus, boolean).apply()
    }

    fun isTransparentStatusDisabled(): Boolean {
        return getSharedPreferences().getBoolean(transparentStatus, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setIconSize(@IntRange(from = 50, to = 600) size: Int) {
        getSharedPreferences().edit().putInt(iconSize, size).apply()
    }

    fun getIconSize(): Int {
        return getSharedPreferences().getInt(iconSize, 400)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColoredIconShadowsState(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(coloredIconShadows, boolean).apply()
    }

    fun getColoredIconShadows(): Boolean {
        return getSharedPreferences().getBoolean(coloredIconShadows, true)
    }
}