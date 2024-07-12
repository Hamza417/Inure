package app.simple.inure.preferences

object ReceiversPreferences {

    const val RECEIVERS_SEARCH = "receivers_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(RECEIVERS_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(RECEIVERS_SEARCH, false)
    }
}
