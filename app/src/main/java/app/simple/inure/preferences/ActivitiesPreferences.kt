package app.simple.inure.preferences

object ActivitiesPreferences {

    private const val activitySearch = "activities_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setActivitySearch(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(activitySearch, boolean).apply()
    }

    fun isActivitySearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(activitySearch, false)
    }
}