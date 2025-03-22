package app.simple.inure.preferences

import app.simple.inure.BuildConfig
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

/**
 * All app preferences
 */
object MainPreferences {

    private const val LAUNCH_COUNT = "main_app_launch_count"
    private const val DAY_NIGHT_MODE = "is_day_night_mode"
    private const val APP_LANGUAGE = "current_language_locale"
    private const val CHANGE_LOG_REMINDER = "change_log_reminder"
    private const val FIRST_LAUNCH_DATE = "first_launch_date"
    private const val UNLOCKER_WARNING_COUNT = "unlocker_warning_count"
    private const val IS_APP_FULL_VERSION_ENABLED = "is_full_version_enabled"
    private const val DISCLAIMER_AGREED = "disclaimer_agreed"
    private const val IS_RATE_REMINDER_SHOWN = "is_rate_reminder_shown_2"

    // ---------------------------------------------------------------------------------------------------------- //

    private fun setLaunchCount(value: Int) {
        getSharedPreferences().edit().putInt(LAUNCH_COUNT, value).apply()
    }

    private fun getLaunchCount(): Int {
        return getSharedPreferences().getInt(LAUNCH_COUNT, 0)
    }

    fun incrementLaunchCount() {
        setLaunchCount(getLaunchCount() + 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    // Day/Night Auto
    fun setDayNight(value: Boolean) {
        getSharedPreferences().edit().putBoolean(DAY_NIGHT_MODE, value).apply()
    }

    fun isDayNightOn(): Boolean {
        return getSharedPreferences().getBoolean(DAY_NIGHT_MODE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppLanguage(locale: String) {
        getSharedPreferences().edit().putString(APP_LANGUAGE, locale).apply()
    }

    fun getAppLanguage(): String? {
        return getSharedPreferences().getString(APP_LANGUAGE, "default")
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun getFirstLaunchDateLegacy(): Long {
        return getSharedPreferences().getLong(FIRST_LAUNCH_DATE, System.currentTimeMillis())
    }

    fun isFullVersionEnabledLegacy(): Boolean {
        return getSharedPreferences().getBoolean(IS_APP_FULL_VERSION_ENABLED, false)
    }

    fun getUnlockerWarningCountLegacy(): Int {
        return getSharedPreferences().getInt(UNLOCKER_WARNING_COUNT, 0)
    }

    fun removeLegacyPreferences() {
        getSharedPreferences().edit().remove(FIRST_LAUNCH_DATE).apply()
        getSharedPreferences().edit().remove(IS_APP_FULL_VERSION_ENABLED).apply()
        getSharedPreferences().edit().remove(UNLOCKER_WARNING_COUNT).apply()
    }

    fun addLegacyPreferences() {
        getSharedPreferences().edit().putLong(FIRST_LAUNCH_DATE, TrialPreferences.getFirstLaunchDate()).apply()
        getSharedPreferences().edit().putBoolean(IS_APP_FULL_VERSION_ENABLED, TrialPreferences.isAppFullVersionEnabled()).apply()
        TrialPreferences.setLegacyMigrated(value = false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setChangeLogReminder(value: Int) {
        getSharedPreferences().edit().putInt(CHANGE_LOG_REMINDER, value).apply()
    }

    private fun getChangeLogReminder(): Int {
        return getSharedPreferences().getInt(CHANGE_LOG_REMINDER, BuildConfig.VERSION_CODE)
    }

    fun shouldShowChangeLogReminder(): Boolean {
        return getChangeLogReminder() < BuildConfig.VERSION_CODE
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDisclaimerAgreed(value: Boolean) {
        getSharedPreferences().edit().putBoolean(DISCLAIMER_AGREED, value).apply()
    }

    fun isDisclaimerAgreed(): Boolean {
        return getSharedPreferences().getBoolean(DISCLAIMER_AGREED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShowRateReminder(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_RATE_REMINDER_SHOWN, value).apply()
    }

    fun isShowRateReminder(): Boolean {
        return getSharedPreferences().getBoolean(IS_RATE_REMINDER_SHOWN, true)
    }
}
