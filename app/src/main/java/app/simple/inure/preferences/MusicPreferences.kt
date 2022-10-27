package app.simple.inure.preferences

object MusicPreferences {

    const val searchKeyword = "music_search_keyword"
    const val lastMusicId = "last_music_id"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchKeyword(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(searchKeyword, value).apply()
    }

    fun getSearchKeyword(): String {
        return SharedPreferences.getSharedPreferences().getString(searchKeyword, "") ?: ""
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastMusicId(value: Long) {
        SharedPreferences.getSharedPreferences().edit().putLong(lastMusicId, value).apply()
    }

    fun getLastMusicId(): Long {
        return SharedPreferences.getSharedPreferences().getLong(lastMusicId, 0)
    }
}