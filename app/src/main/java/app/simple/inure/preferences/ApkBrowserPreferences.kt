package app.simple.inure.preferences

import app.simple.inure.popups.apks.PopupApksCategory
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object ApkBrowserPreferences {

    const val loadSplitIcon = "load_split_icon"
    const val appFilter = "app_filter"

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
}