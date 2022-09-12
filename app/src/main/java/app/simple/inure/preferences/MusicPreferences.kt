package app.simple.inure.preferences

object MusicPreferences {

    const val searchKeyword = "music_search_keyword"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchKeyword(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(searchKeyword, value).apply()
    }

    fun getSearchKeyword(): String {
        return SharedPreferences.getSharedPreferences().getString(searchKeyword, "") ?: ""
    }

}