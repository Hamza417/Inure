package app.simple.inure.preferences

object ServicesPreferences {

    const val SERVICES_SEARCH = "services_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(SERVICES_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(SERVICES_SEARCH, false)
    }
}
