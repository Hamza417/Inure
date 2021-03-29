package app.simple.inure.preferences

import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort
import org.jetbrains.annotations.NotNull

/**
 * All app preferences
 */
object MainPreferences {

    private const val launchCount = "launch_count"
    private const val dayNightMode = "is_day_night_mode"
    private const val theme = "current_theme"
    private const val appLanguage = "current_language_locale"
    private const val appCornerRadius = "corner_radius"
    const val appFont = "type_face"
    private const val sizeType = "size_type"
    const val sortStyle = "sort_style"
    const val isSortingReversed = "is_sorting_reversed"
    const val listAppsCategory = "list_apps_category"

    fun setLaunchCount(value: Int) {
        getSharedPreferences().edit().putInt(launchCount, value).apply()
    }

    fun getLaunchCount(): Int {
        return getSharedPreferences().getInt(launchCount, 0)
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
        return getSharedPreferences().getInt(appCornerRadius, 60)
    }

    fun setAppFont(@NonNull font: String) {
        getSharedPreferences().edit().putString(appFont, font).apply()
    }

    fun getAppFont(): String {
        return getSharedPreferences().getString(appFont, TypeFaceTextView.JOST)!!
    }

    fun setSizeType(@NonNull font: String) {
        getSharedPreferences().edit().putString(sizeType, font).apply()
    }

    fun getSizeType(): String {
        return getSharedPreferences().getString(sizeType, "SI")!!
    }

    fun setSortStyle(@NonNull style: String) {
        getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    fun setReverseSorting(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    fun setListAppCategory(@NonNull category: String) {
        getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getListAppCategory(): String {
        return getSharedPreferences().getString(listAppsCategory, AppCategoryPopup.BOTH)!!
    }
}