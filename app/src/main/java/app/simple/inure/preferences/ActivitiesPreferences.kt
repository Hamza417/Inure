package app.simple.inure.preferences

object ActivitiesPreferences {

    const val ACTIVITY_SEARCH = "activities_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(ACTIVITY_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(ACTIVITY_SEARCH, false)
    }
}
