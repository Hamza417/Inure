package app.simple.inure.preferences

import app.simple.inure.BuildConfig
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort

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
    const val sortStyle = "sort_style"
    const val isSortingReversed = "is_sorting_reversed"
    const val listAppsCategory = "list_apps_category"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLaunchCount(value: Int) {
        getSharedPreferences().edit().putInt(launchCount, value).apply()
    }

    fun getLaunchCount(): Int {
        return getSharedPreferences().getInt(launchCount, 0)
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

    fun setSortStyle(style: String) {
        getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(category: String) {
        getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(listAppsCategory, PopupAppsCategory.BOTH)!!
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
}