package app.simple.inure.util

import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FileSizeHelper.toLength
import java.util.Locale

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
     * Sorts the [PackageInfo] [ArrayList] by
     * relevance (as in search results), impl your own
     * if you want to use this. The class itself does not
     * implement this sorting method.
     */
    const val RELEVANCE = "relevance"

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
            TARGET_SDK -> {
                this.sortByTargetSdk(reverse)
            }
            MIN_SDK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.sortByMinSdk(reverse)
                }
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
            TARGET_SDK -> {
                (this as ArrayList).sortByTargetSdk(reverse)
            }
            MIN_SDK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    (this as ArrayList).sortByMinSdk(reverse)
                }
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
                it.safeApplicationInfo.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.safeApplicationInfo.name.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list package name
     */
    private fun ArrayList<PackageInfo>.sortBySize(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                if (it.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    it.safeApplicationInfo.sourceDir.toLength()
                } else {
                    it.safeApplicationInfo.sourceDir.getDirectoryLength() + it.safeApplicationInfo.sourceDir.toLength()
                }
            }
        } else {
            this.sortBy {
                if (it.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    it.safeApplicationInfo.sourceDir.toLength()
                } else {
                    it.safeApplicationInfo.sourceDir.getDirectoryLength() + it.safeApplicationInfo.sourceDir.toLength()
                }
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

    /**
     * sort application list by target sdk
     */
    private fun ArrayList<PackageInfo>.sortByTargetSdk(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.safeApplicationInfo.targetSdkVersion
            }
        } else {
            this.sortBy {
                it.safeApplicationInfo.targetSdkVersion
            }
        }
    }

    /**
     * sort application list by min sdk
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun ArrayList<PackageInfo>.sortByMinSdk(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.safeApplicationInfo.minSdkVersion
            }
        } else {
            this.sortBy {
                it.safeApplicationInfo.minSdkVersion
            }
        }
    }
}
