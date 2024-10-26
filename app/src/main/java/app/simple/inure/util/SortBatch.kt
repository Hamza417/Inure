package app.simple.inure.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FileSizeHelper.toLength
import java.util.*

object SortBatch {

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
     * Sort the given [ArrayList] in various ways
     *
     * @param type use [SortBatch.NAME] constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun ArrayList<BatchPackageInfo>.getSortedList(type: String, reverse: Boolean) {
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
     * @param type use [SortBatch.NAME] constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun MutableList<BatchPackageInfo>.getSortedList(type: String, reverse: Boolean) {
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
    private fun ArrayList<BatchPackageInfo>.sortByName(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.safeApplicationInfo.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageInfo.safeApplicationInfo.name.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list package name
     */
    private fun ArrayList<BatchPackageInfo>.sortBySize(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                if (it.packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    it.packageInfo.safeApplicationInfo.sourceDir.toLength()
                } else {
                    it.packageInfo.safeApplicationInfo.sourceDir.getDirectoryLength() + it.packageInfo.safeApplicationInfo.sourceDir.toLength()
                }
            }
        } else {
            this.sortBy {
                if (it.packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    it.packageInfo.safeApplicationInfo.sourceDir.toLength()
                } else {
                    it.packageInfo.safeApplicationInfo.sourceDir.getDirectoryLength() + it.packageInfo.safeApplicationInfo.sourceDir.toLength()
                }
            }
        }
    }

    /**
     * sort application list size
     */
    private fun ArrayList<BatchPackageInfo>.sortByPackageName(reverse: Boolean) {
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
    private fun ArrayList<BatchPackageInfo>.sortByInstallDate(reverse: Boolean) {
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
    private fun ArrayList<BatchPackageInfo>.sortByUpdateDate(reverse: Boolean) {
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
    private fun ArrayList<BatchPackageInfo>.sortByTargetSdk(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.safeApplicationInfo.targetSdkVersion
            }
        } else {
            this.sortBy {
                it.packageInfo.safeApplicationInfo.targetSdkVersion
            }
        }
    }

    /**
     * sort application list by min sdk
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun ArrayList<BatchPackageInfo>.sortByMinSdk(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.packageInfo.safeApplicationInfo.minSdkVersion
            }
        } else {
            this.sortBy {
                it.packageInfo.safeApplicationInfo.minSdkVersion
            }
        }
    }
}
