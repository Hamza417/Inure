package app.simple.inure.preferences

import app.simple.inure.util.SortMusic

object MusicPreferences {

    const val searchKeyword = "music_search_keyword"
    const val lastMusicId = "last_music_id"
    const val musicSort = "music_sort"
    const val musicSortReverse = "music_sort_reverse"
    const val musicRepeat = "music_repeat"
    const val musicPosition = "music_position"
    const val fromSearch = "from_search"

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

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicRepeat(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(musicRepeat, value).apply()
    }

    fun getMusicRepeat(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(musicRepeat, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicPosition(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(musicPosition, value).commit()
    }

    fun getMusicPosition(): Int {
        return SharedPreferences.getSharedPreferences().getInt(musicPosition, 0)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFromSearch(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(fromSearch, value).apply()
    }

    fun getFromSearch(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(fromSearch, false)
    }
}