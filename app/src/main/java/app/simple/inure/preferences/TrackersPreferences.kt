package app.simple.inure.preferences

object TrackersPreferences {

    const val isTrackersFullList = "full_classes_list_for_trackers"

    /* ---------------------------------------------------------------------------------------------- */

    fun isFullClassesLis(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isTrackersFullList, false)
    }

    fun setFullClassesList(value: Boolean): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putBoolean(isTrackersFullList, value).commit()
    }
}