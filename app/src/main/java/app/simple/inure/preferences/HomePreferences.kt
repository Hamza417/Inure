package app.simple.inure.preferences

import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object HomePreferences {

    const val homeMenuLayout = "home_menu_layout"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setMenuLayout(layout: Int) {
        getSharedPreferences().edit().putInt(homeMenuLayout, layout).apply()
    }

    fun getMenuLayout(): Int {
        return getSharedPreferences().getInt(homeMenuLayout, PopupMenuLayout.GRID)
    }
}