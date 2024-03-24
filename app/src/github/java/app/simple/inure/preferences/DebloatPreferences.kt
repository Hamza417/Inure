package app.simple.inure.preferences

import app.simple.inure.constants.DebloatSortConstants
import app.simple.inure.constants.SortConstant
import app.simple.inure.sort.DebloatSort

object DebloatPreferences {

    const val SORT = "debloat_sort"
    const val SORTING_STYLE = "debloat_sorting_style"
    const val APPLICATION_TYPE = "debloat_application_type"
    const val LIST_TYPE = "debloat_list_type"
    const val REMOVAL_TYPE = "debloat_removal_type"
    const val STATE = "debloat_state"

    private const val SEARCH_KEYWORD = "debloat_search_keyword"

    fun setSortBy(sortBy: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(SORT, sortBy).apply()
    }

    fun getSortBy(): Int {
        return SharedPreferences.getSharedPreferences().getInt(SORT, DebloatSort.SORT_BY_NAME)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setSortingStyle(sortingStyle: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.SORTING_STYLE, sortingStyle).apply()
    }

    fun getSortingStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(this.SORTING_STYLE, SortConstant.ASCENDING)!!
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setApplicationType(applicationType: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.APPLICATION_TYPE, applicationType).apply()
    }

    fun getApplicationType(): String {
        return SharedPreferences.getSharedPreferences().getString(this.APPLICATION_TYPE, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setListType(listType: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(this.LIST_TYPE, listType).apply()
    }

    fun getListType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(this.LIST_TYPE, DebloatSortConstants.ALL_LIST)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setRemovalType(removalType: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(this.REMOVAL_TYPE, removalType).apply()
    }

    fun getRemovalType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(this.REMOVAL_TYPE, DebloatSortConstants.ALL_REMOVAL)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setState(state: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(this.STATE, state).apply()
    }

    fun getState(): Int {
        return SharedPreferences.getSharedPreferences().getInt(this.STATE, DebloatSortConstants.ALL_STATE)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setSearchKeyword(keyword: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.SEARCH_KEYWORD, keyword).apply()
    }

    fun getSearchKeyword(): String {
        return SharedPreferences.getSharedPreferences().getString(this.SEARCH_KEYWORD, "")!!
    }
}
