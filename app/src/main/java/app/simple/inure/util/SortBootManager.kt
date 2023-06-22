package app.simple.inure.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import app.simple.inure.models.BootManagerModel
import app.simple.inure.preferences.BootManagerPreferences
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import java.util.*

object SortBootManager {

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
     * Sorts the [PackageInfo] [ArrayList] by
     * apps min sdk
     */
    const val MIN_SDK = "min_sdk"

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun ArrayList<BootManagerModel>.getSortedList() {
        when (BootManagerPreferences.getSortStyle()) {
            NAME -> {
                this.sortByName(BootManagerPreferences.isSortingReversed())
            }
            PACKAGE_NAME -> {
                this.sortByPackageName(BootManagerPreferences.isSortingReversed())
            }
            SIZE -> {
                this.sortBySize(BootManagerPreferences.isSortingReversed())
            }
            INSTALL_DATE -> {
                this.sortByInstallDate(BootManagerPreferences.isSortingReversed())
            }
            UPDATE_DATE -> {
                this.sortByUpdateDate(BootManagerPreferences.isSortingReversed())
            }
            TARGET_SDK -> {
                this.sortByTargetSdk(BootManagerPreferences.isSortingReversed())
            }
            MIN_SDK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.sortByMinSdk(BootManagerPreferences.isSortingReversed())
                }
            }
            else -> {
                BootManagerPreferences.setSortStyle(NAME)
                throw IllegalArgumentException("use default sorting constants to sort the list, " +
                                                       "default sorting style is NAME")
            }
        }
    }

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun MutableList<BootManagerModel>.getSortedList() {
        when (BootManagerPreferences.getSortStyle()) {
            NAME -> {
                (this as ArrayList).sortByName(BootManagerPreferences.isSortingReversed())
            }
            PACKAGE_NAME -> {
                (this as ArrayList).sortByPackageName(BootManagerPreferences.isSortingReversed())
            }
            SIZE -> {
                (this as ArrayList).sortBySize(BootManagerPreferences.isSortingReversed())
            }
            INSTALL_DATE -> {
                (this as ArrayList).sortByInstallDate(BootManagerPreferences.isSortingReversed())
            }
            UPDATE_DATE -> {
                (this as ArrayList).sortByUpdateDate(BootManagerPreferences.isSortingReversed())
            }
            TARGET_SDK -> {
                (this as ArrayList).sortByTargetSdk(BootManagerPreferences.isSortingReversed())
            }
            MIN_SDK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    (this as ArrayList).sortByMinSdk(BootManagerPreferences.isSortingReversed())
                }
            }
            else -> {
                BootManagerPreferences.setSortStyle(NAME)
                throw IllegalArgumentException("use default sorting constants to sort the list, " +
                                                       "default sorting style is NAME")
            }
        }
    }

    /**
     * sort application list name
     */
    private fun ArrayList<BootManagerModel>.sortByName(reverse: Boolean) {
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
    private fun ArrayList<BootManagerModel>.sortBySize(reverse: Boolean) {
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
    private fun ArrayList<BootManagerModel>.sortByPackageName(reverse: Boolean) {
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
     * sort application list by install date
     */
    private fun ArrayList<BootManagerModel>.sortByInstallDate(reverse: Boolean) {
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
    private fun ArrayList<BootManagerModel>.sortByUpdateDate(reverse: Boolean) {
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
    private fun ArrayList<BootManagerModel>.sortByTargetSdk(reverse: Boolean) {
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

    /**
     * sort application list by min sdk
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun ArrayList<BootManagerModel>.sortByMinSdk(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.applicationInfo.minSdkVersion
            }
        } else {
            this.sortBy {
                it.packageInfo.applicationInfo.minSdkVersion
            }
        }
    }
}