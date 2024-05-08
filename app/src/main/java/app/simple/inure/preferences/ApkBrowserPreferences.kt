package app.simple.inure.preferences

import app.simple.inure.constants.SortConstant
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortApks

object ApkBrowserPreferences {

    const val LOAD_SPLIT_ICON = "load_split_icon"
    const val SORT_STYLE = "apk_sort_style"
    const val REVERSED = "apk_sort_reversed"
    const val APK_FILTER = "apk_filter"
    const val EXTERNAL_STORAGE = "apk_external_storage"

    private const val SEARCH_KEYWORD = "apk_search_keyword"

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isLoadSplitIcon(): Boolean {
        return getSharedPreferences().getBoolean(LOAD_SPLIT_ICON, false)
    }

    fun setLoadSplitIcon(value: Boolean) {
        getSharedPreferences().edit().putBoolean(LOAD_SPLIT_ICON, value).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getSortStyle(): String {
        return getSharedPreferences().getString(SORT_STYLE, SortApks.NAME)!!
    }

    fun setSortStyle(category: String) {
        getSharedPreferences().edit().putString(SORT_STYLE, category).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isReverseSorting(): Boolean {
        return getSharedPreferences().getBoolean(REVERSED, false)
    }

    fun setReverseSorting(value: Boolean) {
        getSharedPreferences().edit().putBoolean(REVERSED, value).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getSearchKeyword(): String {
        return getSharedPreferences().getString(SEARCH_KEYWORD, "")!!
    }

    fun setSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(SEARCH_KEYWORD, keyword).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun getApkFilter(): Int {
        return getSharedPreferences().getInt(APK_FILTER, SortConstant.ALL_APKS)
    }

    fun setApkFilter(filter: Int) {
        getSharedPreferences().edit().putInt(APK_FILTER, filter).apply()
    }

    // ------------------------------------------------------------------------------------------------------------------ //

    fun isExternalStorage(): Boolean {
        return getSharedPreferences().getBoolean(EXTERNAL_STORAGE, false)
    }

    fun setExternalStorage(value: Boolean) {
        getSharedPreferences().edit().putBoolean(EXTERNAL_STORAGE, value).apply()
    }
}
