package app.simple.inure.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import java.util.*

object SortBatteryOptimization {

    /**
     * Sorts the [PackageInfo] [ArrayList] by
     * [ApplicationInfo.name]
     */
    const val NAME = "name"

    /**
     * Sorts the [PackageInfo] [ArrayList] by
     * [PackageInfo.packageName]
     */
    const val PACKAGE_NAME = "package_name"

    /**
     * Sorts the [PackageInfo] [ArrayList] by
     * apps directory size
     */
    const val SIZE = "size"

    /**
     * Sorts the [PackageInfo] [ArrayList] by
     * apps install date
     */
    const val INSTALL_DATE = "install_date"

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @param type use [Sort.NAME] constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun ArrayList<BatteryOptimizationModel>.getSortedList() {
        when (BatteryOptimizationPreferences.getBatteryOptimizationSortStyle()) {
            NAME -> {
                this.sortByName(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            PACKAGE_NAME -> {
                this.sortByPackageName(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            SIZE -> {
                this.sortBySize(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            INSTALL_DATE -> {
                this.sortByInstallDate(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @param type use [Sort.NAME] constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun MutableList<BatteryOptimizationModel>.getSortedList(type: String, reverse: Boolean) {
        when (BatteryOptimizationPreferences.getBatteryOptimizationSortStyle()) {
            NAME -> {
                (this as ArrayList).sortByName(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            PACKAGE_NAME -> {
                (this as ArrayList).sortByPackageName(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            SIZE -> {
                (this as ArrayList).sortBySize(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            INSTALL_DATE -> {
                (this as ArrayList).sortByInstallDate(BatteryOptimizationPreferences.isBatteryOptimizationSortingReversed())
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * sort application list name
     */
    private fun ArrayList<BatteryOptimizationModel>.sortByName(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.applicationInfo.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageInfo.applicationInfo.name.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list package name
     */
    private fun ArrayList<BatteryOptimizationModel>.sortBySize(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.applicationInfo.sourceDir.getDirectoryLength()
            }
        } else {
            this.sortBy {
                it.packageInfo.applicationInfo.sourceDir.getDirectoryLength()
            }
        }
    }

    /**
     * sort application list size
     */
    private fun ArrayList<BatteryOptimizationModel>.sortByPackageName(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.packageName.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageInfo.packageName.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list alphabetically
     */
    private fun ArrayList<BatteryOptimizationModel>.sortByInstallDate(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.firstInstallTime
            }
        } else {
            this.sortBy {
                it.packageInfo.firstInstallTime
            }
        }
    }

}