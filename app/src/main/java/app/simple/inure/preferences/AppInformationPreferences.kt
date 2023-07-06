package app.simple.inure.preferences

object AppInformationPreferences {

    const val metaMenuState = "is_meta_menu_folded"
    const val actionMenuState = "is_action_menu_folder"
    const val miscMenuState = "is_misc_menu_folded"
    const val menuLayout = "app_info_menu_layout"
    const val metaMenuLayout = "app_info_meta_menu_layout"
    const val actionMenuLayout = "app_info_action_menu_layout"
    const val miscMenuLayout = "app_info_misc_menu_layout"

    const val MENU_LAYOUT_HORIZONTAL = 1
    const val MENU_LAYOUT_GRID = 0

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

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(menuLayout, value).apply()
    }

    fun getMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(menuLayout, 1)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMetaMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(metaMenuLayout, value).apply()
    }

    fun getMetaMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(metaMenuLayout, MENU_LAYOUT_GRID)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setActionMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(actionMenuLayout, value).apply()
    }

    fun getActionMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(actionMenuLayout, MENU_LAYOUT_GRID)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMiscMenuLayout(value: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(miscMenuLayout, value).apply()
    }

    fun getMiscMenuLayout(): Int {
        return SharedPreferences.getSharedPreferences().getInt(miscMenuLayout, MENU_LAYOUT_GRID)
    }
}