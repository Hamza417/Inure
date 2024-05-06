package app.simple.inure.preferences

import app.simple.inure.util.AppUtils
import app.simple.inure.util.CalendarUtils
import java.util.Date

object TrialPreferences {

    private const val MAX_TRIAL_DAYS = 15

    private const val FIRST_LAUNCH = "first_launch_"
    private const val IS_APP_FULL_VERSION_ENABLED = "is_full_version_"
    private const val UNLOCKER_WARNING_COUNT = "unlocker_warning_count_"
    private const val IS_LEGACY_MIGRATED = "is_legacy_migrated_"
    private const val IS_UNLOCKER_VERIFICATION_REQUIRED = "is_unlocker_verification_required_"
    private const val LAST_VERIFICATION_DATE = "last_verification_date_"

    const val HAS_LICENSE_KEY = "has_license_key"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFirstLaunchDate(date: Long) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putLong(FIRST_LAUNCH, date).apply()
    }

    fun getFirstLaunchDate(): Long {
        return SharedPreferences.getEncryptedSharedPreferences().getLong(FIRST_LAUNCH, -1)
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
        SharedPreferences.getEncryptedSharedPreferences().edit().putInt(UNLOCKER_WARNING_COUNT, value).apply()
    }

    fun getUnlockerWarningCount(): Int {
        return SharedPreferences.getEncryptedSharedPreferences().getInt(UNLOCKER_WARNING_COUNT, 0)
    }

    fun incrementUnlockerWarningCount() {
        setUnlockerWarningCount(getUnlockerWarningCount() + 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFullVersion(value: Boolean): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(IS_APP_FULL_VERSION_ENABLED, value).commit()
    }

    fun isAppFullVersionEnabled(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_APP_FULL_VERSION_ENABLED, false) ||
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
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_APP_FULL_VERSION_ENABLED, false)
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
        SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(IS_LEGACY_MIGRATED, value).apply()
    }

    private fun isLegacyMigrated(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_LEGACY_MIGRATED, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setHasLicenceKey(hasLicence: Boolean) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(HAS_LICENSE_KEY, hasLicence).apply()
    }

    fun hasLicenceKey(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(HAS_LICENSE_KEY, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUnlockerVerificationRequired(value: Boolean): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().edit().putBoolean(IS_UNLOCKER_VERIFICATION_REQUIRED, value).commit()
    }

    fun isUnlockerVerificationRequired(): Boolean {
        return SharedPreferences.getEncryptedSharedPreferences().getBoolean(IS_UNLOCKER_VERIFICATION_REQUIRED, true) || AppUtils.isPlayFlavor()
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastVerificationDate(date: Long) {
        SharedPreferences.getEncryptedSharedPreferences().edit().putLong(LAST_VERIFICATION_DATE, date).apply()
    }

    fun getLastVerificationDate(): Long {
        return SharedPreferences.getEncryptedSharedPreferences().getLong(LAST_VERIFICATION_DATE, -1L)
    }
}
