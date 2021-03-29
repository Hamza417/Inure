package app.simple.inure.util

import android.content.Context
import android.content.pm.ApplicationInfo
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import java.util.*

object Sort {

    /**
     * Sorts the [ApplicationInfo] [ArrayList] by
     * [ApplicationInfo.name]
     */
    const val NAME = "name"

    /**
     * Sorts the [ApplicationInfo] [ArrayList] by
     * [ApplicationInfo.packageName]
     */
    const val PACKAGE_NAME = "package_name"

    /**
     * Sorts the [ApplicationInfo] [ArrayList] by
     * apps directory size
     */
    const val SIZE = "size"

    /**
     * Sorts the [ApplicationInfo] [ArrayList] by
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
    fun ArrayList<ApplicationInfo>.getSortedList(type: String, context: Context) {
        when (type) {
            NAME -> {
                this.sortByName()
            }
            PACKAGE_NAME -> {
                this.sortByPackageName()
            }
            SIZE -> {
                this.sortBySize(context)
            }
            INSTALL_DATE -> {
                this.sortByInstallDate(context)
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * sort application list name
     */
    private fun ArrayList<ApplicationInfo>.sortByName() {
        return if (MainPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.name.toLowerCase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.name.toLowerCase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list package name
     */
    private fun ArrayList<ApplicationInfo>.sortBySize(context: Context) {
        return if (MainPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.sourceDir.getDirectoryLength()
            }
        } else {
            this.sortBy {
                it.sourceDir.getDirectoryLength()
            }
        }
    }

    /**
     * sort application list size
     */
    private fun ArrayList<ApplicationInfo>.sortByPackageName() {
        return if (MainPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.packageName.toLowerCase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageName.toLowerCase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list alphabetically
     */
    private fun ArrayList<ApplicationInfo>.sortByInstallDate(context: Context) {
        return if (MainPreferences.isReverseSorting()) {
            this.sortByDescending {
                context.packageManager.getPackageInfo(it.packageName, 0).firstInstallTime
            }
        } else {
            this.sortBy {
                context.packageManager.getPackageInfo(it.packageName, 0).firstInstallTime
            }
        }
    }
}