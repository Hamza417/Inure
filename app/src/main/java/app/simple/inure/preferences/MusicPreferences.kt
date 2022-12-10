package app.simple.inure.preferences

import app.simple.inure.util.SortMusic

object MusicPreferences {

    const val searchKeyword = "music_search_keyword"
    const val lastMusicId = "last_music_id"
    const val musicSort = "music_sort"
    const val musicSortReverse = "music_sort_reverse"

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

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicSort(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(musicSort, value).apply()
    }

    fun getMusicSort(): String {
        return SharedPreferences.getSharedPreferences().getString(musicSort, SortMusic.NAME) ?: SortMusic.NAME
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicSortReverse(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(musicSortReverse, value).apply()
    }

    fun getMusicSortReverse(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(musicSortReverse, false)
    }
}