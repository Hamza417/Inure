package app.simple.inure.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.FileSizeHelper.toLength
import app.simple.inure.util.FileSizeHelper.toSize

object InfoStripUtils {

    fun TypeFaceTextView.setAppInfo(packageInfo: PackageInfo) {
        text = context.getAppInfo(packageInfo)
    }

    fun Context.getAppInfo(packageInfo: PackageInfo): StringBuilder {
        buildString {
            // Version
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_VERSION)) {
                append(packageInfo.versionName)
            }

            // Type
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_TYPE)) {
                if ((packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appendOR(getString(R.string.user))
                } else {
                    appendOR(getString(R.string.system))
                }
            }

            // Size
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_SIZE)) {
                if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    appendOR(packageInfo.safeApplicationInfo.sourceDir.toSize())
                } else {
                    appendOR((packageInfo.safeApplicationInfo.splitSourceDirs!!.getDirectorySize() +
                            packageInfo.safeApplicationInfo.sourceDir.toLength()).toSize())
                }
            }

            // State
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_STATE)) {
                if (packageInfo.isInstalled().not()) {
                    appendOR(getString(R.string.uninstalled))
                } else {
                    if (packageInfo.safeApplicationInfo.enabled) {
                        appendOR(getString(R.string.enabled))
                    } else {
                        appendOR(getString(R.string.disabled))
                    }
                }
            }

            // Category
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_CATEGORY)) {
                    appendOR(MetaUtils.getCategory(packageInfo.safeApplicationInfo.category, this@getAppInfo))
                }
            }

            // Target SDK
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_PACKAGE_TYPE)) {
                if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    appendOR(getString(R.string.apk))
                } else {
                    appendOR(getString(R.string.split_packages))
                }
            }

            // Min SDK
            when {
                FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_MIN_SDK or SortConstant.INFO_TARGET_SDK) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appendOR(packageInfo.safeApplicationInfo.minSdkVersion.toString())
                        append("..")
                        append(packageInfo.safeApplicationInfo.targetSdkVersion)
                    } else {
                        append(packageInfo.safeApplicationInfo.targetSdkVersion)
                    }
                }
                FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_MIN_SDK) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appendOR(packageInfo.safeApplicationInfo.minSdkVersion.toString())
                    }
                }
                FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_TARGET_SDK) -> {
                    appendOR(packageInfo.safeApplicationInfo.targetSdkVersion.toString())
                }
            }

            // Install time
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_INSTALL_DATE)) {
                appendOR(packageInfo.firstInstallTime.toDate())
            }

            // Update time
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_UPDATE_DATE)) {
                appendOR(packageInfo.lastUpdateTime.toDate())
            }

            return this
        }
    }

    fun TypeFaceTextView.setUninstalledAppInfo(packageInfo: PackageInfo) {
        buildString {
            append(packageInfo.versionName)
            append(" | ")

            if ((packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                append(context.getString(R.string.user))
            } else {
                append(context.getString(R.string.system))
            }

            append(" | ")
            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(packageInfo.safeApplicationInfo.sourceDir.toSize())
            } else {
                append((packageInfo.safeApplicationInfo.splitSourceDirs!!.getDirectorySize() +
                        packageInfo.safeApplicationInfo.sourceDir.toLength()).toSize())
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                append(" | ")
                append(MetaUtils.getCategory(packageInfo.safeApplicationInfo.category, context))
            }

            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(" | ")
                append(context.getString(R.string.apk))
            } else {
                append(" | ")
                append(context.getString(R.string.split_packages))
            }

            append(" | ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(packageInfo.safeApplicationInfo.minSdkVersion)
                append("..")
                append(packageInfo.safeApplicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.safeApplicationInfo.targetSdkVersion)
            }

            text = toString()
        }
    }

    fun TypeFaceTextView.setRecentlyInstalledInfo(packageInfo: PackageInfo) {
        buildString {
            append(packageInfo.getApplicationInstallTime(context, FormattingPreferences.getDateFormat()))
            append(" | ")
            //            append(packageInfo.versionName)
            //            append(" | ")
            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(packageInfo.safeApplicationInfo.sourceDir.toSize())
            } else {
                append((packageInfo.safeApplicationInfo.splitSourceDirs!!.getDirectorySize() +
                        packageInfo.safeApplicationInfo.sourceDir.toLength()).toSize())
            }
            append(" | ")
            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(context.getString(R.string.apk))
            } else {
                append(context.getString(R.string.split_packages))
            }

            append(" | ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(packageInfo.safeApplicationInfo.minSdkVersion)
                append("..")
                append(packageInfo.safeApplicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.safeApplicationInfo.targetSdkVersion)
            }

        }.also { text = it }
    }

    fun TypeFaceTextView.setRecentlyUpdatedInfo(packageInfo: PackageInfo) {
        buildString {
            append(packageInfo.lastUpdateTime.toDate())
            append(" | ")
            //            append(packageInfo.versionName)
            //            append(" | ")
            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(packageInfo.safeApplicationInfo.sourceDir.toSize())
            } else {
                append((packageInfo.safeApplicationInfo.splitSourceDirs!!.getDirectorySize() +
                        packageInfo.safeApplicationInfo.sourceDir.toLength()).toSize())
            }
            append(" | ")
            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(context.getString(R.string.apk))
            } else {
                append(context.getString(R.string.split_packages))
            }

            append(" | ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(packageInfo.safeApplicationInfo.minSdkVersion)
                append("..")
                append(packageInfo.safeApplicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.safeApplicationInfo.targetSdkVersion)
            }

        }.also { text = it }
    }

    private fun StringBuilder.appendOR(value: String) {
        if (isNotEmpty()) {
            append(" | ")
        }
        append(value)
    }
}
