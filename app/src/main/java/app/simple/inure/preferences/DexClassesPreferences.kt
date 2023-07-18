package app.simple.inure.preferences

object DexClassesPreferences {

    const val dexSearch = "is_search_visible"

    // --------------------------------------------------------------------------------------------- //

    fun setSearchVisible(isVisible: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(dexSearch, isVisible).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(dexSearch, true)
    }
}