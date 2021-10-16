package app.simple.inure.preferences

object AppInfoPanelPreferences {

    const val metaMenuState = "is_meta_menu_folded"
    const val actionMenuState = "is_action_menu_folder"
    const val miscMenuState = "is_misc_menu_folded"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMetaMenuFold(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(metaMenuState, boolean).apply()
    }

    fun isMetaMenuFolded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(metaMenuState, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setActionMenuFold(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(actionMenuState, boolean).apply()
    }

    fun isActionMenuFolded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(actionMenuState, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMiscMenuFold(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(miscMenuState, boolean).apply()
    }

    fun isMiscMenuFolded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(miscMenuState, false)
    }
}