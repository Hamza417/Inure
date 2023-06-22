package app.simple.inure.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FileSizeHelper.toLength
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
     * Sorts the [PackageInfo] [ArrayList] by
     * apps update date
     */
    const val UPDATE_DATE = "update_date"

    /**
     * Sorts the [PackageInfo] [ArrayList] by
     * apps target sdk
     */
    const val TARGET_SDK = "target_sdk"

    /**
     * Sort the given [ArrayList] in various ways
     */
    fun ArrayList<BatteryOptimizationModel>.getSortedList() {
        when (BatteryOptimizationPreferences.getSortStyle()) {
            NAME -> {
                this.sortByName(BatteryOptimizationPreferences.isSortingReversed())
            }
            PACKAGE_NAME -> {
                this.sortByPackageName(BatteryOptimizationPreferences.isSortingReversed())
            }
            SIZE -> {
                this.sortBySize(BatteryOptimizationPreferences.isSortingReversed())
            }
            INSTALL_DATE -> {
                this.sortByInstallDate(BatteryOptimizationPreferences.isSortingReversed())
            }
            UPDATE_DATE -> {
                this.sortByUpdateDate(BatteryOptimizationPreferences.isSortingReversed())
            }
            TARGET_SDK -> {
                this.sortByTargetSdk(BatteryOptimizationPreferences.isSortingReversed())
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * Sort the given [ArrayList] in various ways
     */
    fun MutableList<BatteryOptimizationModel>.getSortedList() {
        when (BatteryOptimizationPreferences.getSortStyle()) {
            NAME -> {
                (this as ArrayList).sortByName(BatteryOptimizationPreferences.isSortingReversed())
            }
            PACKAGE_NAME -> {
                (this as ArrayList).sortByPackageName(BatteryOptimizationPreferences.isSortingReversed())
            }
            SIZE -> {
                (this as ArrayList).sortBySize(BatteryOptimizationPreferences.isSortingReversed())
            }
            INSTALL_DATE -> {
                (this as ArrayList).sortByInstallDate(BatteryOptimizationPreferences.isSortingReversed())
            }
            UPDATE_DATE -> {
                (this as ArrayList).sortByUpdateDate(BatteryOptimizationPreferences.isSortingReversed())
            }
            TARGET_SDK -> {
                (this as ArrayList).sortByTargetSdk(BatteryOptimizationPreferences.isSortingReversed())
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
                if (it.packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    it.packageInfo.applicationInfo.sourceDir.toLength()
                } else {
                    it.packageInfo.applicationInfo.sourceDir.getDirectoryLength() + it.packageInfo.applicationInfo.sourceDir.toLength()
                }
            }
        } else {
            this.sortBy {
                if (it.packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    it.packageInfo.applicationInfo.sourceDir.toLength()
                } else {
                    it.packageInfo.applicationInfo.sourceDir.getDirectoryLength() + it.packageInfo.applicationInfo.sourceDir.toLength()
                }
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

    /**
     * sort application list by update date
     */
    private fun ArrayList<BatteryOptimizationModel>.sortByUpdateDate(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.lastUpdateTime
            }
        } else {
            this.sortBy {
                it.packageInfo.lastUpdateTime
            }
        }
    }

    /**
     * sort application list by target sdk
     */
    private fun ArrayList<BatteryOptimizationModel>.sortByTargetSdk(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.applicationInfo.targetSdkVersion
            }
        } else {
            this.sortBy {
                it.packageInfo.applicationInfo.targetSdkVersion
            }
        }
    }
}