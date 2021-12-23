package app.simple.inure.preferences

object PermissionPreferences {

    const val permissionSearch = "permissions_search"
    const val labelType = "permission_label_type"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSearchVisibility(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(permissionSearch, boolean).apply()
    }

    fun isSearchVisible(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(permissionSearch, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    /**
     * True for ID
     * False for Descriptive
     */
    fun setLabelType(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(labelType, boolean).apply()
    }

    fun getLabelType(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(labelType, true)
    }
}