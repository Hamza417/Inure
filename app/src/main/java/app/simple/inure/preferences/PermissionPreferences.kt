package app.simple.inure.preferences

object PermissionPreferences {

    const val PERMISSION_SEARCH = "permissions_search"
    const val LABEL_TYPE = "permission_label_type"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(PERMISSION_SEARCH, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(PERMISSION_SEARCH, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    /**
     * True for ID
     * False for Descriptive
     */
    fun setLabelType(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(LABEL_TYPE, boolean).apply()
    }

    fun getLabelType(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(LABEL_TYPE, true)
    }
}
