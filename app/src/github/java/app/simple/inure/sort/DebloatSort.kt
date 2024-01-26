package app.simple.inure.sort

import app.simple.inure.constants.SortConstant
import app.simple.inure.models.Bloat
import app.simple.inure.preferences.DebloatPreferences

object DebloatSort {

    const val SORT_BY_NAME = 0
    const val SORT_BY_PACKAGE_NAME = 1

    fun ArrayList<Bloat>.getSortedList() {
        when (DebloatPreferences.getSortBy()) {
            SORT_BY_NAME -> {
                sortByName()
            }
            SORT_BY_PACKAGE_NAME -> {
                sortByPackageName()
            }
        }
    }

    private fun ArrayList<Bloat>.sortByName() {
        when (DebloatPreferences.getSortingStyle()) {
            SortConstant.ASCENDING -> {
                sortBy { it.packageInfo.applicationInfo.name }
            }
            SortConstant.DESCENDING -> {
                sortByDescending { it.packageInfo.applicationInfo.name }
            }
        }
    }

    private fun ArrayList<Bloat>.sortByPackageName() {
        when (DebloatPreferences.getSortingStyle()) {
            SortConstant.ASCENDING -> {
                sortBy { it.packageInfo.packageName }
            }
            SortConstant.DESCENDING -> {
                sortByDescending { it.packageInfo.packageName }
            }
        }
    }
}
