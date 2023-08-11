package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getEncryptedSharedPreferences
import app.simple.inure.util.AppUtils
import app.simple.inure.util.CalendarUtils
import java.util.Date

object TrialPreferences {

    private const val MAX_TRIAL_DAYS = 15

    private const val firstLaunch = "first_launch_"
    private const val isAppFullVersionEnabled = "is_full_version_"
    private const val unlockerWarningCount = "unlocker_warning_count_"
    private const val isLegacyMigrated = "is_legacy_migrated_"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFirstLaunchDate(date: Long) {
        getEncryptedSharedPreferences().edit().putLong(firstLaunch, date).apply()
    }

    fun getFirstLaunchDate(): Long {
        return getEncryptedSharedPreferences().getLong(firstLaunch, -1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun getDaysLeft(): Int {
        return kotlin.runCatching {
            MAX_TRIAL_DAYS - CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday())
                .coerceAtLeast(0).coerceAtMost(MAX_TRIAL_DAYS)
        }.getOrElse {
            -1
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    private fun setUnlockerWarningCount(value: Int) {
        getEncryptedSharedPreferences().edit().putInt(unlockerWarningCount, value).apply()
    }

    fun getUnlockerWarningCount(): Int {
        return getEncryptedSharedPreferences().getInt(unlockerWarningCount, 0)
    }

    fun incrementUnlockerWarningCount() {
        setUnlockerWarningCount(getUnlockerWarningCount() + 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFullVersion(value: Boolean): Boolean {
        return getEncryptedSharedPreferences().edit().putBoolean(isAppFullVersionEnabled, value).commit()
    }

    fun isAppFullVersionEnabled(): Boolean {
        return getEncryptedSharedPreferences().getBoolean(isAppFullVersionEnabled, false) ||
                CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS ||
                AppUtils.isBetaFlavor()
    }

    fun isWithinTrialPeriod(): Boolean {
        return CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
    }

    fun isTrialWithoutFull(): Boolean {
        return CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
                && !isAppFullVersionEnabled()
    }

    fun isFullVersion(): Boolean {
        return getEncryptedSharedPreferences().getBoolean(isAppFullVersionEnabled, false) ||
                AppUtils.isBetaFlavor()
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun reset() {
        setFirstLaunchDate(-1)
        setUnlockerWarningCount(0)
        setFullVersion(false)
    }

    fun resetUnlockerWarningCount() {
        setUnlockerWarningCount(0)
    }

    fun migrateLegacy() {
        if (!isLegacyMigrated()) {
            setFirstLaunchDate(MainPreferences.getFirstLaunchDateLegacy())
            setUnlockerWarningCount(MainPreferences.getUnlockerWarningCountLegacy())
            setFullVersion(MainPreferences.isFullVersionEnabledLegacy())

            setLegacyMigrated(true)
            MainPreferences.removeLegacyPreferences()
        }
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLegacyMigrated(value: Boolean) {
        getEncryptedSharedPreferences().edit().putBoolean(isLegacyMigrated, value).apply()
    }

    fun isLegacyMigrated(): Boolean {
        return getEncryptedSharedPreferences().getBoolean(isLegacyMigrated, false)
    }
}