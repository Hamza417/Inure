package app.simple.inure.preferences

object OperationsPreferences {

    const val operationsSearch = "operations_search"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(operationsSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(operationsSearch, false)
    }
}