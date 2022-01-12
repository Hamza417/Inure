package app.simple.inure.preferences

import android.net.Uri
import androidx.annotation.NonNull
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort
import org.jetbrains.annotations.NotNull

/**
 * All app preferences
 */
object MainPreferences {

    private const val launchCount = "launch_count"
    private const val dayNightMode = "is_day_night_mode"
    private const val appLanguage = "current_language_locale"
    private const val storagePermissionUri = "storage_permission_uri"
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

    // ---------------------------------------------------------------------------------------------------------- //

    // Day/Night Auto
    fun setDayNight(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(dayNightMode, value).apply()
    }

    fun isDayNightOn(): Boolean {
        return getSharedPreferences().getBoolean(dayNightMode, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppLanguage(@NonNull locale: String) {
        getSharedPreferences().edit().putString(appLanguage, locale).apply()
    }

    fun getAppLanguage(): String? {
        return getSharedPreferences().getString(appLanguage, "default")
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSortStyle(@NonNull style: String) {
        getSharedPreferences().edit().putString(sortStyle, style).apply()
    }

    fun getSortStyle(): String {
        return getSharedPreferences().getString(sortStyle, Sort.NAME)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setReverseSorting(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSortingReversed, value).apply()
    }

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(isSortingReversed, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppsCategory(@NonNull category: String) {
        getSharedPreferences().edit().putString(listAppsCategory, category).apply()
    }

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(listAppsCategory, PopupAppsCategory.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setStoragePermissionUri(@NonNull uri: Uri) {
        getSharedPreferences().edit().putString(storagePermissionUri, uri.toString()).apply()
    }

    fun getStoragePermissionUri(): String? {
        return getSharedPreferences().getString(storagePermissionUri, null)
    }
}