package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object SearchPreferences {
    private const val lastSearchKeyword = "last_search_keyword"

    fun setLastSearchKeyword(keyword: String) {
        getSharedPreferences().edit().putString(lastSearchKeyword, keyword).apply()
    }

    fun getLastSearchKeyword(): String {
        return getSharedPreferences().getString(lastSearchKeyword, "")!!
    }
}
