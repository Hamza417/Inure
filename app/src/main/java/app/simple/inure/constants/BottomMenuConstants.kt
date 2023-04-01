package app.simple.inure.constants

import app.simple.inure.R
import app.simple.inure.preferences.ConfigurationPreferences

object BottomMenuConstants {

    private val divider = Pair(-1, -1)

    private val genericBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val allAppsBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_sort, R.string.sort),
            Pair(R.drawable.ic_filter, R.string.filter),
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
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
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val bootManagerBottomMenuItems: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_sort, R.string.sort),
            Pair(R.drawable.ic_filter, R.string.filter),
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
            Pair(R.drawable.ic_sort, R.string.sort),
            divider,
            Pair(R.drawable.shuffle, R.string.shuffle),
            Pair(R.drawable.ic_play, R.string.play),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val batchUnselectedMenu: ArrayList<Pair<Int, Int>> = arrayListOf(
            Pair(R.drawable.ic_select_all, R.string.select_all),
            Pair(R.drawable.ic_sort, R.string.sort),
            Pair(R.drawable.ic_filter, R.string.filter),
            Pair(R.drawable.ic_settings, R.string.preferences),
            divider,
            Pair(R.drawable.ic_search, R.string.search),
    )

    private val batchMenuNonRoot: ArrayList<Pair<Int, Int>> by lazy {
        arrayListOf(
                Pair(R.drawable.ic_delete, R.string.delete),
                // Pair(R.drawable.ic_share, R.string.share),
                Pair(R.drawable.ic_downloading, R.string.extract),
                Pair(R.drawable.ic_text_snippet, R.string.data),
                divider,
                Pair(R.drawable.ic_checklist, R.string.checklist),
                Pair(R.drawable.ic_select_all, R.string.select_all),
                divider,
                Pair(R.drawable.ic_sort, R.string.sort),
                Pair(R.drawable.ic_filter, R.string.filter),
                Pair(R.drawable.ic_settings, R.string.preferences),
                divider,
                Pair(R.drawable.ic_search, R.string.search),
        )
    }

    private val batchMenuRoot: ArrayList<Pair<Int, Int>> by lazy {
        arrayListOf(
                Pair(R.drawable.ic_settings_power, R.string.battery),
                divider,
                Pair(R.drawable.ic_delete, R.string.delete),
                // Pair(R.drawable.ic_share, R.string.share),
                Pair(R.drawable.ic_downloading, R.string.extract),
                Pair(R.drawable.ic_text_snippet, R.string.data),
                divider,
                Pair(R.drawable.ic_checklist, R.string.checklist),
                Pair(R.drawable.ic_select_all, R.string.select_all),
                divider,
                Pair(R.drawable.ic_sort, R.string.sort),
                Pair(R.drawable.ic_filter, R.string.filter),
                Pair(R.drawable.ic_settings, R.string.preferences),
                divider,
                Pair(R.drawable.ic_search, R.string.search),
        )
    }

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

    val apkBrowserMenu: ArrayList<Pair<Int, Int>> by lazy {
        arrayListOf(
                Pair(R.drawable.ic_refresh, R.string.refresh),
                divider,
                Pair(R.drawable.ic_settings, R.string.preferences),
                Pair(R.drawable.ic_search, R.string.search),
        )
    }

    val apkBrowserMenuSelection: ArrayList<Pair<Int, Int>> by lazy {
        arrayListOf(
                Pair(R.drawable.ic_delete, R.string.delete),
                Pair(R.drawable.ic_send, R.string.send),
                divider,
                Pair(R.drawable.ic_refresh, R.string.refresh),
                divider,
                Pair(R.drawable.ic_search, R.string.search),
        )
    }

    // *********************************************************************************************** //

    @Suppress("UNCHECKED_CAST")
    fun getGenericBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return genericBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllAppsBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return allAppsBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getSensorsBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return sensorsBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBootManagerBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return bootManagerBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getMusicBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return musicBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBatchMenu(): ArrayList<Pair<Int, Int>> {
        return if (ConfigurationPreferences.isUsingRoot() || ConfigurationPreferences.isUsingShizuku()) {
            batchMenuRoot.clone() as ArrayList<Pair<Int, Int>>
        } else {
            batchMenuNonRoot.clone() as ArrayList<Pair<Int, Int>>
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getBatchUnselectedMenu(): ArrayList<Pair<Int, Int>> {
        return batchUnselectedMenu.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getNotesFunctionMenu(): ArrayList<Pair<Int, Int>> {
        return notesFunctionMenu.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getStackTracesBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return stackTracesBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getTerminalCommandsBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return terminalCommandsBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getUninstalledBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return uninstalledBottomMenuItems.clone() as ArrayList<Pair<Int, Int>>
    }
}