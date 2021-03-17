package app.simple.inure.preferences

import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

/**
 * All app preferences
 */
object MainPreferences {

    private const val launchCount = "launch_count"
    private const val dayNightMode = "is_day_night_mode"
    private const val licenseStatus = "license_status"
    private const val theme = "current_theme"
    private const val notifications = "is_push_notifications_on"
    private const val screenOn = "keep_the_screen_on"
    private const val appLanguage = "current_language_locale"
    private const val appCornerRadius = "corner_radius"
    private const val appFont = "type_face"
    private const val sizeType = "size_type"

    fun setLaunchCount(value: Int) {
        getSharedPreferences().edit().putInt(launchCount, value).apply()
    }

    fun getLaunchCount(): Int {
        return getSharedPreferences().getInt(launchCount, 0)
    }

    fun setScreenOn(value: Boolean) {
        getSharedPreferences().edit().putBoolean(screenOn, value).apply()
    }

    fun isScreenOn(): Boolean {
        return getSharedPreferences().getBoolean(screenOn, false)
    }

    /**
     * @param value for storing theme preferences
     * 1 - Light
     * 2 - Dark
     * 3 - System
     * 4 - Day/Night
     */
    fun setTheme(value: Int) {
        getSharedPreferences().edit().putInt(theme, value).apply()
    }

    fun getTheme(): Int {
        return getSharedPreferences().getInt(theme, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // Day/Night Auto
    fun setDayNight(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(dayNightMode, value).apply()
    }

    fun isDayNightOn(): Boolean {
        return getSharedPreferences().getBoolean(dayNightMode, false)
    }

    fun setLicenseStatus(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(licenseStatus, value).apply()
    }

    fun getLicenceStatus(): Boolean {
        return getSharedPreferences().getBoolean(licenseStatus, false)
    }

    fun setNotifications(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(notifications, value).apply()
    }

    fun isNotificationOn(): Boolean {
        return getSharedPreferences().getBoolean(notifications, true)
    }

    fun setAppLanguage(@NonNull locale: String) {
        getSharedPreferences().edit().putString(appLanguage, locale).apply()
    }

    fun getAppLanguage(): String? {
        return getSharedPreferences().getString(appLanguage, "default")
    }

    fun setCornerRadius(@IntRange(from = 25, to = 400) radius: Int) {
        getSharedPreferences().edit().putInt(appCornerRadius, radius / 5).apply()
    }

    fun getCornerRadius(): Int {
        return getSharedPreferences().getInt(appCornerRadius, 30)
    }

    fun setAppFont(@NonNull font: String) {
        getSharedPreferences().edit().putString(appFont, font).apply()
    }

    fun getAppFont(): String {
        return getSharedPreferences().getString(appFont, "lato")!!
    }

    fun setSizeType(@NonNull font: String) {
        getSharedPreferences().edit().putString(sizeType, font).apply()
    }

    fun getSizeType(): String {
        return getSharedPreferences().getString(sizeType, "SI")!!
    }
}