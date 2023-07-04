package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortApks

object ApkBrowserPreferences {

    const val loadSplitIcon = "load_split_icon"
    const val sortStyle = "apk_sort_style"
    const val reversed = "apk_sort_reversed"
    const val apkFilter = "apk_filter"
    const val searchKeyword = "apk_search_keyword"
    const val nomedia = "nomedia_apks"

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isLoadSplitIcon(): Boolean {
        return getSharedPreferences().getBoolean(loadSplitIcon, false)
    }

    fun setLoadSplitIcon(value: Boolean) {
        getSharedPreferences().edit().putBoolean(loadSplitIcon, value).apply()
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

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getSearchKeyword(): String {
        return getSharedPreferences().getString(searchKeyword, "")!!
    }

    fun setSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(searchKeyword, keyword).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getApkFilter(): Int {
        return getSharedPreferences().getInt(apkFilter, SortConstant.ALL_APKS)
    }

    fun setApkFilter(filter: Int) {
        getSharedPreferences().edit().putInt(apkFilter, filter).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isNomediaEnabled(): Boolean {
        return getSharedPreferences().getBoolean(nomedia, true)
    }

    fun setNomediaEnabled(value: Boolean) {
        getSharedPreferences().edit().putBoolean(nomedia, value).apply()
    }
}