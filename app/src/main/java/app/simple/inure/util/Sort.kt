package app.simple.inure.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import java.util.*

object Sort {

    /**
     * Sorts the [PackageInfo] [ArrayList] by
     * apps update date
     */
    const val UPDATE_DATE = "update_date"

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
    fun ArrayList<PackageInfo>.getSortedList(type: String, reverse: Boolean) {
        when (type) {
            NAME -> {
                this.sortByName(reverse)
            }
            PACKAGE_NAME -> {
                this.sortByPackageName(reverse)
            }
            SIZE -> {
                this.sortBySize(reverse)
            }
            INSTALL_DATE -> {
                this.sortByInstallDate(reverse)
            }
            UPDATE_DATE -> {
                this.sortByUpdateDate(reverse)
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
    fun MutableList<PackageInfo>.getSortedList(type: String, reverse: Boolean) {
        when (type) {
            NAME -> {
                (this as ArrayList).sortByName(reverse)
            }
            PACKAGE_NAME -> {
                (this as ArrayList).sortByPackageName(reverse)
            }
            SIZE -> {
                (this as ArrayList).sortBySize(reverse)
            }
            INSTALL_DATE -> {
                (this as ArrayList).sortByInstallDate(reverse)
            }
            UPDATE_DATE -> {
                (this as ArrayList).sortByUpdateDate(reverse)
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * sort application list name
     */
    private fun ArrayList<PackageInfo>.sortByName(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.applicationInfo.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.applicationInfo.name.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list package name
     */
    private fun ArrayList<PackageInfo>.sortBySize(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.applicationInfo.sourceDir.getDirectoryLength()
            }
        } else {
            this.sortBy {
                it.applicationInfo.sourceDir.getDirectoryLength()
            }
        }
    }

    /**
     * sort application list size
     */
    private fun ArrayList<PackageInfo>.sortByPackageName(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageName.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageName.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list by install date
     */
    private fun ArrayList<PackageInfo>.sortByInstallDate(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.firstInstallTime
            }
        } else {
            this.sortBy {
                it.firstInstallTime
            }
        }
    }

    /**
     * sort application list by update date
     */
    private fun ArrayList<PackageInfo>.sortByUpdateDate(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.lastUpdateTime
            }
        } else {
            this.sortBy {
                it.lastUpdateTime
            }
        }
    }
}