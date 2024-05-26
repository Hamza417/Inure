package app.simple.inure.constants

import app.simple.inure.R

@Suppress("UNCHECKED_CAST")
object BottomMenuConstants {

    private val divider = Pair(-1, -1)
    private val refresh = Pair(R.drawable.ic_refresh, R.string.refresh)

    private val genericBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            refresh,
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val allAppsBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            refresh,
            divider,
            Pair(R.drawable.ic_filter, R.string.filter),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val sensorsBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_sort, R.string.sort),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val uninstalledBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_info, R.string.info),
            divider,
            Pair(R.drawable.ic_refresh, R.string.refresh),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val bootManagerBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            // Pair(R.drawable.ic_sort, R.string.sort),
            refresh,
            Pair(R.drawable.ic_filter, R.string.filter),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val stackTracesBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_clear_all, R.string.clear),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val terminalCommandsBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_add, R.string.add),
            // Pair(R.drawable.ic_clear_all, R.string.clear),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val musicBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            refresh,
            Pair(R.drawable.ic_sort, R.string.sort),
            divider,
            Pair(R.drawable.shuffle, R.string.shuffle),
            Pair(R.drawable.ic_play, R.string.play),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val batchUnselectedMenu: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_checklist, R.string.checklist),
            Pair(R.drawable.ic_select_all, R.string.select_all),
            divider,
            refresh,
            Pair(R.drawable.ic_filter, R.string.filter),
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val batchSelectedMenu: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_extension, R.string.actions),
            divider,
            Pair(R.drawable.ic_checklist, R.string.checklist),
            Pair(R.drawable.ic_select_all, R.string.select_all),
            divider,
            refresh,
            Pair(R.drawable.ic_filter, R.string.filter),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val notesFunctionMenu: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_format_bold, R.string.bold),
            Pair(R.drawable.ic_format_italic, R.string.italic),
            Pair(R.drawable.ic_format_underlined, R.string.underline),
            Pair(R.drawable.ic_format_strikethrough, R.string.strikethrough),
            divider,
            Pair(R.drawable.ic_format_superscript, R.string.superscript),
            Pair(R.drawable.ic_format_subscript, R.string.subscript),
            divider,
            Pair(R.drawable.ic_format_paint, R.string.highlight),
            divider,
            Pair(R.drawable.ic_watch_later, R.string.date),
    )

    private val apkBrowserMenu_: ArrayList<Pair<Int, Int>> by lazy {
        arrayListOf(
                Pair(R.drawable.ic_refresh, R.string.refresh),
                divider,
                // Pair(R.drawable.ic_sort, R.string.sort),
                Pair(R.drawable.ic_filter, R.string.filter),
                divider,
                Pair(R.drawable.ic_settings, R.string.preferences),
                Pair(R.drawable.ic_search, R.string.search),
        )
    }

    private val apkBrowserMenuSelection_: ArrayList<Pair<Int, Int>> by lazy {
        arrayListOf(
                Pair(R.drawable.ic_delete, R.string.delete),
                Pair(R.drawable.ic_send, R.string.send),
                divider,
                Pair(R.drawable.ic_refresh, R.string.refresh),
                divider,
                // Pair(R.drawable.ic_sort, R.string.sort),
                Pair(R.drawable.ic_filter, R.string.filter),
                divider,
                Pair(R.drawable.ic_settings, R.string.preferences),
                Pair(R.drawable.ic_search, R.string.search),
        )
    }

    private val debloatMenuNoSelection: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_help, R.string.help),
            Pair(R.drawable.ic_select_all, R.string.select_all),
            divider,
            Pair(R.drawable.ic_refresh, R.string.refresh),
            divider,
            Pair(R.drawable.ic_filter, R.string.filter),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val debloatMenuSelection: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_recycling, R.string.debloat),
            Pair(R.drawable.ic_restore, R.string.restore),
            Pair(R.drawable.ic_help, R.string.help),
            divider,
            Pair(R.drawable.ic_select_all, R.string.select_all),
            Pair(R.drawable.ic_checklist, R.string.checklist),
            divider,
            Pair(R.drawable.ic_refresh, R.string.refresh),
            divider,
            Pair(R.drawable.ic_filter, R.string.filter),
            divider,
            Pair(R.drawable.ic_settings, R.string.preferences),
            Pair(R.drawable.ic_search, R.string.search),
    )

    // *********************************************************************************************** //

    fun getGenericBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return genericBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getAllAppsBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return allAppsBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getSensorsBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return sensorsBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getBootManagerBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return bootManagerBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getMusicBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return musicBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getBatchUnselectedMenu(): ArrayList<Pair<Int, Int>> {
        return batchUnselectedMenu.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getBatchSelectedMenu(): ArrayList<Pair<Int, Int>> {
        return batchSelectedMenu.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST", "unused")
    fun getNotesFunctionMenu(): ArrayList<Pair<Int, Int>> {
        return notesFunctionMenu.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getStackTracesBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return stackTracesBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getTerminalCommandsBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return terminalCommandsBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getUninstalledBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return uninstalledBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getApkBrowserMenu(): ArrayList<Pair<Int, Int>> {
        return apkBrowserMenu_.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getApkBrowserMenuSelection(): ArrayList<Pair<Int, Int>> {
        return apkBrowserMenuSelection_.clone() as ArrayList<Pair<Int, Int>>
    }

    fun getDebloatMenu(isSelected: Boolean): ArrayList<Pair<Int, Int>> {
        return if (isSelected) {
            debloatMenuSelection.clone() as ArrayList<Pair<Int, Int>>
        } else {
            debloatMenuNoSelection.clone() as ArrayList<Pair<Int, Int>>
        }
    }
}
