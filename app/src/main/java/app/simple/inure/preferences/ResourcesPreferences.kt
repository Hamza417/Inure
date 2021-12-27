package app.simple.inure.preferences

object ResourcesPreferences {

    const val resourcesSearch = "resources_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(resourcesSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(resourcesSearch, false)
    }
}