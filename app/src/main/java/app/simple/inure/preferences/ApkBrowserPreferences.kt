package app.simple.inure.preferences

import app.simple.inure.popups.apks.PopupApksCategory
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortApks

object ApkBrowserPreferences {

    const val loadSplitIcon = "load_split_icon"
    const val appFilter = "apk_app_filter"
    const val sortStyle = "apk_sort_style"
    const val reversed = "apk_sort_reversed"

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isLoadSplitIcon(): Boolean {
        return getSharedPreferences().getBoolean(loadSplitIcon, false)
    }

    fun setLoadSplitIcon(value: Boolean) {
        getSharedPreferences().edit().putBoolean(loadSplitIcon, value).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getAppsCategory(): String {
        return getSharedPreferences().getString(appFilter, PopupApksCategory.BOTH)!!
    }

    fun setAppsCategory(category: String) {
        getSharedPreferences().edit().putString(appFilter, category).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getSortStyle(): String {
        return getSharedPreferences().getString(sortStyle, SortApks.NAME)!!
    }

    fun setSortStyle(category: String) {
        getSharedPreferences().edit().putString(sortStyle, category).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(reversed, false)
    }

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(reversed, value).apply()
    }
}