package app.simple.inure.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.TypeFace

object AppearancePreferences {

    private const val APP_CORNER_RADIUS = "view_corner_radius"
    private const val ICON_SHADOWS = "icon_shadows"
    private const val LAST_LIGHT_THEME = "last_light_theme"
    private const val LAST_DARK_THEME = "last_dark_theme"
    private const val COLORED_ICON_SHADOWS = "icon_shadows_colored"
    private const val IS_MATERIAL_YOU_ACCENT = "is_material_you_accent"
    private const val ACCENT_COLOR_ON_BOTTOM_MENU = "accent_color_on_bottom_menu"
    private const val PICKED_ACCENT_COLOR = "picked_accent_color"

    const val IS_CUSTOM_COLOR = "is_custom_color"
    const val THEME = "current_app_theme"
    const val ACCENT_COLOR = "app_accent_color"
    private const val ACCENT_COLOR_LIGHT = "app_accent_color_light"
    private const val APP_FONT = "type_face"
    const val ACCENT_ON_NAV = "accent_color_on_nav_bar"
    const val ICON_SIZE = "app_icon_size"

    var minIconSize = 0
    var maxIconSize = 75

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColor(@ColorInt int: Int): Boolean {
        return getSharedPreferences().edit().putInt(ACCENT_COLOR, int).commit()
    }

    @ColorInt
    fun getAccentColor(): Int {
        // Possibly solve the no color issue?
        return getSharedPreferences().getInt(ACCENT_COLOR, 0xFFFF8C68.toInt())
    }

    fun getAccentColorStateList(): ColorStateList {
        return ColorStateList.valueOf(getAccentColor())
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setPickedAccentColor(@ColorInt int: Int): Boolean {
        return getSharedPreferences().edit().putInt(PICKED_ACCENT_COLOR, int).commit()
    }

    @ColorInt
    fun getPickedAccentColor(): Int {
        return getSharedPreferences().getInt(PICKED_ACCENT_COLOR, 0xFFFF8C68.toInt())
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCustomColor(boolean: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(IS_CUSTOM_COLOR, boolean).commit()
    }

    fun isCustomColor(): Boolean {
        return getSharedPreferences().getBoolean(IS_CUSTOM_COLOR, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    @Suppress("unused")
    fun setAccentColorLight(@ColorInt int: Int) {
        getSharedPreferences().edit().putInt(ACCENT_COLOR_LIGHT, int).apply()
    }

    @Suppress("unused")
    @ColorInt
    fun getAccentColorLight(context: Context): Int {
        return if (ThemeUtils.isNightMode(context.resources)) {
            getSharedPreferences().getInt(ACCENT_COLOR_LIGHT, ColorUtils.changeAlpha(getAccentColor(), 0.8F))
        } else {
            getSharedPreferences().getInt(ACCENT_COLOR_LIGHT, ColorUtils.changeAlpha(getAccentColor(), 0.4F))
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColorOnBottomMenu(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(ACCENT_COLOR_ON_BOTTOM_MENU, boolean).apply()
    }

    fun isAccentColorOnBottomMenu(): Boolean {
        return getSharedPreferences().getBoolean(ACCENT_COLOR_ON_BOTTOM_MENU, false)
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
        return getSharedPreferences().edit().putInt(THEME, value).commit()
    }

    fun getTheme(): Int {
        return getSharedPreferences().getInt(THEME, ThemeConstants.FOLLOW_SYSTEM)
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
        getSharedPreferences().edit().putInt(LAST_DARK_THEME, value).apply()
    }

    fun getLastDarkTheme(): Int {
        return getSharedPreferences().getInt(LAST_DARK_THEME, ThemeConstants.DARK_THEME)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastLightTheme(value: Int) {
        getSharedPreferences().edit().putInt(LAST_LIGHT_THEME, value).apply()
    }

    fun getLastLightTheme(): Int {
        return getSharedPreferences().getInt(LAST_LIGHT_THEME, ThemeConstants.LIGHT_THEME)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppFont(font: String): Boolean {
        return getSharedPreferences().edit().putString(APP_FONT, font).commit()
    }

    fun getAppFont(): String {
        return getSharedPreferences().getString(APP_FONT, TypeFace.AUTO)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCornerRadius(radius: Float) {
        getSharedPreferences().edit().putFloat(APP_CORNER_RADIUS, if (radius < 1F) 1F else radius).apply()
    }

    fun getCornerRadius(): Float {
        return getSharedPreferences().getFloat(APP_CORNER_RADIUS, 60F).coerceAtLeast(1F)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setIconShadows(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(ICON_SHADOWS, boolean).apply()
    }

    fun isIconShadowsOn(): Boolean {
        return getSharedPreferences().getBoolean(ICON_SHADOWS, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentOnNavigationBar(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(ACCENT_ON_NAV, boolean).apply()
    }

    fun isAccentOnNavigationBar(): Boolean {
        return getSharedPreferences().getBoolean(ACCENT_ON_NAV, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setIconSize(size: Int) {
        getSharedPreferences().edit().putInt(ICON_SIZE, size).apply()
    }

    @Synchronized
    fun getIconSize(): Int {
        return getSharedPreferences().getInt(ICON_SIZE, maxIconSize.div(4))
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setColoredIconShadowsState(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(COLORED_ICON_SHADOWS, boolean).apply()
    }

    fun getColoredIconShadows(): Boolean {
        return getSharedPreferences().getBoolean(COLORED_ICON_SHADOWS, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    @RequiresApi(Build.VERSION_CODES.S)
    fun setMaterialYouAccent(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_MATERIAL_YOU_ACCENT, boolean).apply()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun isMaterialYouAccent(): Boolean {
        return getSharedPreferences().getBoolean(IS_MATERIAL_YOU_ACCENT, false)
    }
}
