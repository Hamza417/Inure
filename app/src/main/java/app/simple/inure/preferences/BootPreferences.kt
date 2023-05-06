package app.simple.inure.preferences

object BootPreferences {

    const val isSearchVisible = "boot_search_visible"

    // ---------------------------------------------------------------------------------------------------------- //

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isSearchVisible, false)
    }

    fun setSearchVisible(isVisible: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isSearchVisible, isVisible).apply()
    }
}