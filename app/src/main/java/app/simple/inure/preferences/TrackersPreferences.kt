package app.simple.inure.preferences

object TrackersPreferences {

    const val isTrackersFullList = "full_classes_list_for_trackers"
    const val trackersSearch = "is_trackers_search_visible"

    private const val trackerAutomaticMessage = "is_tracker_message_shown_automatically"

    /* ---------------------------------------------------------------------------------------------- */

    fun isFullClassesLis(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isTrackersFullList, false)
    }

    fun setFullClassesList(value: Boolean): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putBoolean(isTrackersFullList, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun isMessageShownAutomatically(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trackerAutomaticMessage, false)
    }

    fun setAutomaticMessage(value: Boolean): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putBoolean(trackerAutomaticMessage, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(trackersSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trackersSearch, false)
    }
}