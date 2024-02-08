package app.simple.inure.preferences

import app.simple.inure.util.CalendarUtils
import java.util.Date

object TrialPreferences {

    private const val MAX_TRIAL_DAYS = 15

    private const val firstLaunch = "first_launch_"
    private const val isAppFullVersionEnabled = "is_full_version_"
    private const val unlockerWarningCount = "unlocker_warning_count_"
    private const val isLegacyMigrated = "is_legacy_migrated_"
    private const val isUnlockerVerificationRequired = "is_unlocker_verification_required_"

    const val hasLicenseKey = "has_license_key"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFirstLaunchDate(date: Long) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putLong(firstLaunch, date).apply()
    }

    fun getFirstLaunchDate(): Long {
        return SharedPreferences.getEncryptedSharedPreferences().getLong(firstLaunch, -1)
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
        SharedPreferences.getEncryptedSharedPreferences().edit().putInt(unlockerWarningCount, value).apply()
    }

    fun getUnlockerWarningCount(): Int {
        return SharedPreferences.getEncryptedSharedPreferences().getInt(unlockerWarningCount, 0)
    }

    fun incrementUnlockerWarningCount() {
        setUnlockerWarningCount(getUnlockerWarningCount() + 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFullVersion(value: Boolean): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(isAppFullVersionEnabled, value).commit()
    }

    fun isAppFullVersionEnabled(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(isAppFullVersionEnabled, false) ||
                CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
    }

    fun isWithinTrialPeriod(): Boolean {
        return CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
    }

    fun isTrialWithoutFull(): Boolean {
        return CalendarUtils.getDaysBetweenTwoDates(Date(getFirstLaunchDate()), CalendarUtils.getToday()) <= MAX_TRIAL_DAYS
                && !isAppFullVersionEnabled()
    }

    fun isFullVersion(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(isAppFullVersionEnabled, false)
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
        SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(isLegacyMigrated, value).apply()
    }

    private fun isLegacyMigrated(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(isLegacyMigrated, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHasLicenceKey(hasLicence: Boolean) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(hasLicenseKey, hasLicence).apply()
    }

    fun hasLicenceKey(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(hasLicenseKey, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUnlockerVerificationRequired(value: Boolean) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(isUnlockerVerificationRequired, value).apply()
    }

    fun isUnlockerVerificationRequired(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(isUnlockerVerificationRequired, true)
    }
}