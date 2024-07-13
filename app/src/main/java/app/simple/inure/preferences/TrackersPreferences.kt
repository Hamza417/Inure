package app.simple.inure.preferences

object TrackersPreferences {

    const val TRACKERS_SEARCH = "is_trackers_search_visible"

    /* ---------------------------------------------------------------------------------------------- */

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(TRACKERS_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(TRACKERS_SEARCH, false)
    }
}
