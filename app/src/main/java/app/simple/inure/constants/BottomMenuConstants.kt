package app.simple.inure.constants

import app.simple.inure.R

object BottomMenuConstants {

    private val allAppsBottomMenuItems = arrayListOf(
            R.drawable.ic_sort,
            R.drawable.ic_filter,
            R.drawable.ic_settings,
            -1,
            R.drawable.ic_search
    )

    private val bootManagerBottomMenuItems = arrayListOf(
            // R.drawable.ic_sort,
            // R.drawable.ic_filter,
            R.drawable.ic_settings,
            -1,
            R.drawable.ic_search
    )

    private val musicBottomMenuItems = arrayListOf(
            R.drawable.ic_sort,
            -1,
            R.drawable.shuffle,
            R.drawable.ic_play,
            -1,
            R.drawable.ic_search
    )

    private val batchActionMenu = arrayListOf(
            R.drawable.ic_delete,
            R.drawable.ic_send,
            R.drawable.ic_downloading,
            R.drawable.ic_checklist,
            -1,
    )

    private val batchMenu = arrayListOf(
            R.drawable.ic_delete,
            // R.drawable.ic_send,
            R.drawable.ic_downloading,
            R.drawable.ic_checklist,
            -1,
            R.drawable.ic_sort,
            R.drawable.ic_filter,
            R.drawable.ic_settings,
            -1,
            R.drawable.ic_search
    )

    private val notesFunctionMenu = arrayListOf(
            R.drawable.ic_format_bold,
            R.drawable.ic_format_italic,
            R.drawable.ic_format_underlined,
            R.drawable.ic_format_strikethrough,
            -1,
            R.drawable.ic_format_superscript,
            R.drawable.ic_format_subscript,
            -1,
            R.drawable.ic_format_paint,
    )

    @Suppress("UNCHECKED_CAST")
    fun getAllAppsBottomMenuItems(): ArrayList<Int> {
        return allAppsBottomMenuItems.clone() as ArrayList<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBootManagerBottomMenuItems(): ArrayList<Int> {
        return bootManagerBottomMenuItems.clone() as ArrayList<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getMusicBottomMenuItems(): ArrayList<Int> {
        return musicBottomMenuItems.clone() as ArrayList<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBatchActionMenu(): ArrayList<Int> {
        return batchActionMenu.clone() as ArrayList<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBatchMenu(): ArrayList<Int> {
        return batchMenu.clone() as ArrayList<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getNotesFunctionMenu(): ArrayList<Int> {
        return notesFunctionMenu.clone() as ArrayList<Int>
    }
}