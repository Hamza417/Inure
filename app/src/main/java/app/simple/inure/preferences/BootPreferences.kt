package app.simple.inure.preferences

object BootPreferences {

    const val IS_SEARCH_VISIBLE = "boot_search_visible"

    // ---------------------------------------------------------------------------------------------------------- //

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_SEARCH_VISIBLE, false)
    }

    fun setSearchVisible(isVisible: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_SEARCH_VISIBLE, isVisible).apply()
    }
}
