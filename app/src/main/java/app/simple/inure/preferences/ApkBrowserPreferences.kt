package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object ApkBrowserPreferences {

    const val loadSplitIcon = "load_split_icon"

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isLoadSplitIcon(): Boolean {
        return getSharedPreferences().getBoolean(loadSplitIcon, false)
    }

    fun setLoadSplitIcon(value: Boolean) {
        getSharedPreferences().edit().putBoolean(loadSplitIcon, value).apply()
    }
}