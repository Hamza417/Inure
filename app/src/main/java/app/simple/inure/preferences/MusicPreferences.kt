package app.simple.inure.preferences

import android.annotation.SuppressLint
import app.simple.inure.util.SortMusic

object MusicPreferences {

    const val SEARCH_KEYWORD = "music_search_keyword"
    const val LAST_MUSIC_ID = "last_music_id"
    const val MUSIC_SORT = "music_sort"
    const val MUSIC_SORT_REVERSE = "music_sort_reverse"
    const val MUSIC_REPEAT = "music_repeat"

    private const val MUSIC_POSITION = "music_position"
    private const val FROM_SEARCH = "from_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchKeyword(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SEARCH_KEYWORD, value).apply()
    }

    fun getSearchKeyword(): String {
        return SharedPreferences.getSharedPreferences().getString(SEARCH_KEYWORD, "") ?: ""
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLastMusicId(value: Long) {
        SharedPreferences.getSharedPreferences().edit().putLong(LAST_MUSIC_ID, value).apply()
    }

    fun getLastMusicId(): Long {
        return SharedPreferences.getSharedPreferences().getLong(LAST_MUSIC_ID, 0)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicSort(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(MUSIC_SORT, value).apply()
    }

    fun getMusicSort(): String {
        return SharedPreferences.getSharedPreferences().getString(MUSIC_SORT, SortMusic.NAME) ?: SortMusic.NAME
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicSortReverse(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(MUSIC_SORT_REVERSE, value).apply()
    }

    fun getMusicSortReverse(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(MUSIC_SORT_REVERSE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMusicRepeat(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(MUSIC_REPEAT, value).apply()
    }

    fun getMusicRepeat(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(MUSIC_REPEAT, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    @SuppressLint("ApplySharedPref")
    fun setMusicPosition(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(MUSIC_POSITION, value).commit()
    }

    fun getMusicPosition(): Int {
        return SharedPreferences.getSharedPreferences().getInt(MUSIC_POSITION, 0)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setFromSearch(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(FROM_SEARCH, value).apply()
    }

    fun getFromSearch(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(FROM_SEARCH, false)
    }
}
