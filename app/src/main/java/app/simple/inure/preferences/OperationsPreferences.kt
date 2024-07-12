package app.simple.inure.preferences

object OperationsPreferences {

    const val OPERATIONS_SEARCH = "operations_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(OPERATIONS_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(OPERATIONS_SEARCH, false)
    }
}
