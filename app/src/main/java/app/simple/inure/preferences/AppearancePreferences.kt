package app.simple.inure.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.TypeFace

object AppearancePreferences {

    private const val appCornerRadius = "view_corner_radius"
    private const val iconShadows = "icon_shadows"
    private const val lastLightTheme = "last_light_theme"
    private const val lastDarkTheme = "last_dark_theme"
    private const val coloredIconShadows = "icon_shadows_colored"
    private const val isMaterialYouAccent = "is_material_you_accent"

    const val isCustomColor = "is_custom_color"
    const val theme = "current_app_theme"
    const val accentColor = "app_accent_color"
    const val accentColorLight = "app_accent_color_light"
    const val appFont = "type_face"
    const val accentOnNav = "accent_color_on_nav_bar"
    const val iconSize = "app_icon_size"

    var minIconSize = 0
    var maxIconSize = 75

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColor(@ColorInt int: Int): Boolean {
        return getSharedPreferences().edit().putInt(accentColor, int).commit()
    }

    @ColorInt
    fun getAccentColor(): Int {
        // Possibly solve the no color issue?
        return getSharedPreferences().getInt(accentColor, Color.parseColor("#ff8c68"))
    }

    fun getAccentColorStateList(): ColorStateList {
        return ColorStateList.valueOf(getAccentColor())
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCustomColor(boolean: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(isCustomColor, boolean).commit()
    }

    fun isCustomColor(): Boolean {
        return getSharedPreferences().getBoolean(isCustomColor, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    @Suppress("unused")
    fun setAccentColorLight(@ColorInt int: Int) {
        getSharedPreferences().edit().putInt(accentColorLight, int).apply()
    }

    @Suppress("unused")
    @ColorInt
    fun getAccentColorLight(context: Context): Int {
        return if (ThemeUtils.isNightMode(context.resources)) {
            getSharedPreferences().getInt(accentColorLight, ColorUtils.darkenColor(getAccentColor(), 0.2F))
        } else {
            getSharedPreferences().getInt(accentColorLight, ColorUtils.lightenColor(getAccentColor(), 0.4F))
        }
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
    fun setTheme(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(theme, value).commit()
    }

    fun getTheme(): Int {
        return getSharedPreferences().getInt(theme, ThemeConstants.FOLLOW_SYSTEM)
    }

    fun migrateMaterialYouTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            if (getTheme() == ThemeConstants.MATERIAL_YOU) {
                setLastDarkTheme(ThemeConstants.MATERIAL_YOU_DARK)
                setLastLightTheme(ThemeConstants.MATERIAL_YOU_LIGHT)
                setTheme(ThemeConstants.FOLLOW_SYSTEM)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastDarkTheme(value: Int) {
        getSharedPreferences().edit().putInt(lastDarkTheme, value).apply()
    }

    fun getLastDarkTheme(): Int {
        return getSharedPreferences().getInt(lastDarkTheme, ThemeConstants.DARK_THEME)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastLightTheme(value: Int) {
        getSharedPreferences().edit().putInt(lastLightTheme, value).apply()
    }

    fun getLastLightTheme(): Int {
        return getSharedPreferences().getInt(lastLightTheme, ThemeConstants.LIGHT_THEME)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppFont(font: String): Boolean {
        return getSharedPreferences().edit().putString(appFont, font).commit()
    }

    fun getAppFont(): String {
        return getSharedPreferences().getString(appFont, TypeFace.AUTO)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCornerRadius(radius: Float) {
        getSharedPreferences().edit().putFloat(appCornerRadius, if (radius < 1F) 1F else radius).apply()
    }

    fun getCornerRadius(): Float {
        return getSharedPreferences().getFloat(appCornerRadius, 60F).coerceAtLeast(1F)
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

    fun setIconSize(size: Int) {
        getSharedPreferences().edit().putInt(iconSize, size).apply()
    }

    @Synchronized
    fun getIconSize(): Int {
        return getSharedPreferences().getInt(iconSize, maxIconSize.div(4))
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColoredIconShadowsState(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(coloredIconShadows, boolean).apply()
    }

    fun getColoredIconShadows(): Boolean {
        return getSharedPreferences().getBoolean(coloredIconShadows, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    @RequiresApi(Build.VERSION_CODES.S)
    fun setMaterialYouAccent(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(isMaterialYouAccent, boolean).apply()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun isMaterialYouAccent(): Boolean {
        return getSharedPreferences().getBoolean(isMaterialYouAccent, false)
    }
}
