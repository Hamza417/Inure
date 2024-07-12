package app.simple.inure.preferences

object AppInformationPreferences {

    const val META_MENU_STATE = "is_meta_menu_folded"
    const val ACTION_MENU_STATE = "is_action_menu_folder"
    const val MISC_MENU_STATE = "is_misc_menu_folded"
    const val MENU_LAYOUT = "app_info_menu_layout"
    const val META_MENU_LAYOUT = "app_info_meta_menu_layout"
    const val ACTION_MENU_LAYOUT = "app_info_action_menu_layout"
    const val MISC_MENU_LAYOUT = "app_info_misc_menu_layout"

    const val MENU_LAYOUT_HORIZONTAL = 1
    const val MENU_LAYOUT_GRID = 0

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMetaMenuFold(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(META_MENU_STATE, boolean).apply()
    }

    fun isMetaMenuFolded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(META_MENU_STATE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setActionMenuFold(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(ACTION_MENU_STATE, boolean).apply()
    }

    fun isActionMenuFolded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(ACTION_MENU_STATE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMiscMenuFold(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(MISC_MENU_STATE, boolean).apply()
    }

    fun isMiscMenuFolded(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(MISC_MENU_STATE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(MENU_LAYOUT, value).apply()
    }

    fun getMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(MENU_LAYOUT, 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMetaMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(META_MENU_LAYOUT, value).apply()
    }

    fun getMetaMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(META_MENU_LAYOUT, MENU_LAYOUT_GRID)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setActionMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(ACTION_MENU_LAYOUT, value).apply()
    }

    fun getActionMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(ACTION_MENU_LAYOUT, MENU_LAYOUT_GRID)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMiscMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(MISC_MENU_LAYOUT, value).apply()
    }

    fun getMiscMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(MISC_MENU_LAYOUT, MENU_LAYOUT_GRID)
    }
}
