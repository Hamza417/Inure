package app.simple.inure.preferences

import app.simple.inure.BuildConfig
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

/**
 * All app preferences
 */
object MainPreferences {

    private const val launchCount = "main_app_launch_count"
    private const val dayNightMode = "is_day_night_mode"
    private const val appLanguage = "current_language_locale"
    private const val changeLogReminder = "change_log_reminder"
    private const val firstLaunchDate = "first_launch_date"
    private const val unlockerWarningCount = "unlocker_warning_count"
    private const val isAppFullVersionEnabled = "is_full_version_enabled"
    private const val disclaimerAgreed = "disclaimer_agreed"
    private const val isRateReminderShown = "is_rate_reminder_shown_2"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLaunchCount(value: Int) {
        getSharedPreferences().edit().putInt(launchCount, value).apply()
    }

    fun getLaunchCount(): Int {
        return getSharedPreferences().getInt(launchCount, 0)
    }

    fun shouldShowRateReminder(): Boolean {
        return getLaunchCount() % (5..10).random() == 0
    }

    fun incrementLaunchCount() {
        setLaunchCount(getLaunchCount() + 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    // Day/Night Auto
    fun setDayNight(value: Boolean) {
        getSharedPreferences().edit().putBoolean(dayNightMode, value).apply()
    }

    fun isDayNightOn(): Boolean {
        return getSharedPreferences().getBoolean(dayNightMode, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppLanguage(locale: String) {
        getSharedPreferences().edit().putString(appLanguage, locale).apply()
    }

    fun getAppLanguage(): String? {
        return getSharedPreferences().getString(appLanguage, "default")
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun getFirstLaunchDateLegacy(): Long {
        return getSharedPreferences().getLong(firstLaunchDate, System.currentTimeMillis())
    }

    fun isFullVersionEnabledLegacy(): Boolean {
        return getSharedPreferences().getBoolean(isAppFullVersionEnabled, false)
    }

    fun getUnlockerWarningCountLegacy(): Int {
        return getSharedPreferences().getInt(unlockerWarningCount, 0)
    }

    fun removeLegacyPreferences() {
        getSharedPreferences().edit().remove(firstLaunchDate).apply()
        getSharedPreferences().edit().remove(isAppFullVersionEnabled).apply()
        getSharedPreferences().edit().remove(unlockerWarningCount).apply()
    }

    fun addLegacyPreferences() {
        getSharedPreferences().edit().putLong(firstLaunchDate, TrialPreferences.getFirstLaunchDate()).apply()
        getSharedPreferences().edit().putBoolean(isAppFullVersionEnabled, TrialPreferences.isAppFullVersionEnabled()).apply()
        getSharedPreferences().edit().putInt(unlockerWarningCount, TrialPreferences.getUnlockerWarningCount()).apply()
        TrialPreferences.setLegacyMigrated(value = false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setChangeLogReminder(value: Int) {
        getSharedPreferences().edit().putInt(changeLogReminder, value).apply()
    }

    private fun getChangeLogReminder(): Int {
        return getSharedPreferences().getInt(changeLogReminder, BuildConfig.VERSION_CODE)
    }

    fun shouldShowChangeLogReminder(): Boolean {
        return getChangeLogReminder() < BuildConfig.VERSION_CODE
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDisclaimerAgreed(value: Boolean) {
        getSharedPreferences().edit().putBoolean(disclaimerAgreed, value).apply()
    }

    fun isDisclaimerAgreed(): Boolean {
        return getSharedPreferences().getBoolean(disclaimerAgreed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShowRateReminder(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isRateReminderShown, value).apply()
    }

    fun isShowRateReminder(): Boolean {
        return getSharedPreferences().getBoolean(isRateReminderShown, true)
    }
}