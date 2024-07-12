package app.simple.inure.preferences

object ProvidersPreferences {

    const val PROVIDERS_SEARCH = "providers_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(PROVIDERS_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(PROVIDERS_SEARCH, false)
    }

}
