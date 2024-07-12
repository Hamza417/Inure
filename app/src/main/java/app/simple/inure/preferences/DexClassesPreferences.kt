package app.simple.inure.preferences

object DexClassesPreferences {

    const val DEX_SEARCH = "is_search_visible"

    // --------------------------------------------------------------------------------------------- //

    fun setSearchVisible(isVisible: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(DEX_SEARCH, isVisible).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(DEX_SEARCH, true)
    }
}
