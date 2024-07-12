package app.simple.inure.preferences

object ResourcesPreferences {

    const val RESOURCES_SEARCH = "resources_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(RESOURCES_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(RESOURCES_SEARCH, false)
    }
}
