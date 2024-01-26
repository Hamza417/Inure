package app.simple.inure.preferences

import app.simple.inure.constants.DebloatSortConstants
import app.simple.inure.constants.SortConstant
import app.simple.inure.sort.DebloatSort

object DebloatPreferences {

    const val sort = "debloat_sort"
    const val sortingStyle = "debloat_sorting_style"
    const val applicationType = "debloat_application_type"
    const val listType = "debloat_list_type"
    const val removalType = "debloat_removal_type"
    const val state = "debloat_state"

    fun setSortBy(sortBy: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(sort, sortBy).apply()
    }

    fun getSortBy(): Int {
        return SharedPreferences.getSharedPreferences().getInt(sort, DebloatSort.SORT_BY_NAME)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setSortingStyle(sortingStyle: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.sortingStyle, sortingStyle).apply()
    }

    fun getSortingStyle(): String {
        return SharedPreferences.getSharedPreferences().getString(this.sortingStyle, SortConstant.ASCENDING)!!
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setApplicationType(applicationType: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.applicationType, applicationType).apply()
    }

    fun getApplicationType(): String {
        return SharedPreferences.getSharedPreferences().getString(this.applicationType, SortConstant.BOTH)!!
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setListType(listType: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(this.listType, listType).apply()
    }

    fun getListType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(this.listType, DebloatSortConstants.ALL_LIST)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setRemovalType(removalType: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(this.removalType, removalType).apply()
    }

    fun getRemovalType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(this.removalType, DebloatSortConstants.ALL_REMOVAL)
    }

    // ---------------------------------------------------------------------------------------------- //

    fun setState(state: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(this.state, state).apply()
    }

    fun getState(): Int {
        return SharedPreferences.getSharedPreferences().getInt(this.state, DebloatSortConstants.ALL_STATE)
    }
}