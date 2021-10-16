package app.simple.inure.preferences

object ServicesPreferences {
    const val servicesSearch = "services_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(servicesSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(servicesSearch, false)
    }
}