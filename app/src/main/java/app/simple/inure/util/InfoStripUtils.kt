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
        val filter = AppsPreferences.getInfoCustomFilter()
        val ai = packageInfo.safeApplicationInfo
        return StringBuilder().apply {
            // Version
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_VERSION)) {
                append(ai.packageName.let { packageInfo.versionName })
            }

            // Type
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_TYPE)) {
                appendOR(getAppTypeString(ai))
            }

            // Size
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_SIZE)) {
                appendOR(getSizeString(ai))
            }

            // State
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_STATE)) {
                appendOR(getStateString(packageInfo))
            }

            // Category
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_CATEGORY)) {
                getCategoryString(ai)?.let { appendOR(it) }
            }

            // Package type (APK/Split)
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_PACKAGE_TYPE)) {
                appendOR(getPackageTypeString(ai))
            }

            // Min/Target SDK
            when {
                FlagUtils.isFlagSet(filter, SortConstant.INFO_MIN_SDK or SortConstant.INFO_TARGET_SDK) -> {
                    appendOR(getSdkRangeString(ai, includeMin = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N))
                }
                FlagUtils.isFlagSet(filter, SortConstant.INFO_MIN_SDK) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appendOR(ai.minSdkVersion.toString())
                    }
                }
                FlagUtils.isFlagSet(filter, SortConstant.INFO_TARGET_SDK) -> {
                    appendOR(ai.targetSdkVersion.toString())
                }
            }

            // Install time
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_INSTALL_DATE)) {
                appendOR(packageInfo.firstInstallTime.toDate())
            }

            // Update time
            if (FlagUtils.isFlagSet(filter, SortConstant.INFO_UPDATE_DATE)) {
                appendOR(packageInfo.lastUpdateTime.toDate())
            }
        }
    }

    fun TypeFaceTextView.setUninstalledAppInfo(packageInfo: PackageInfo) {
        val ai = packageInfo.safeApplicationInfo
        buildString {
            append(packageInfo.versionName)
            append(" | ")
            append(context.getAppTypeString(ai))
            append(" | ")
            append(getSizeString(ai))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getCategoryString(ai)?.let {
                    append(" | ")
                    append(it)
                }
            }
            append(" | ")
            append(context.getPackageTypeString(ai))
            append(" | ")
            append(getSdkRangeString(ai, includeMin = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N))
            text = toString()
        }
    }

    fun TypeFaceTextView.setRecentlyInstalledInfo(packageInfo: PackageInfo) {
        val ai = packageInfo.safeApplicationInfo
        buildString {
            append(packageInfo.getApplicationInstallTime(context, FormattingPreferences.getDateFormat()))
            append(" | ")
            append(getSizeString(ai))
            append(" | ")
            append(context.getPackageTypeString(ai))
            append(" | ")
            append(getSdkRangeString(ai, includeMin = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N))
        }.also { text = it }
    }

    fun TypeFaceTextView.setRecentlyUpdatedInfo(packageInfo: PackageInfo) {
        val ai = packageInfo.safeApplicationInfo
        buildString {
            append(packageInfo.lastUpdateTime.toDate())
            append(" | ")
            append(getSizeString(ai))
            append(" | ")
            append(context.getPackageTypeString(ai))
            append(" | ")
            append(getSdkRangeString(ai, includeMin = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N))
        }.also { text = it }
    }

    // Helpers
    private fun getSizeString(ai: ApplicationInfo): String {
        val splits = ai.splitSourceDirs
        return if (splits.isNullOrEmpty()) {
            ai.sourceDir.toSize()
        } else {
            (splits.getDirectorySize() + ai.sourceDir.toLength()).toSize()
        }
    }

    private fun Context.getPackageTypeString(ai: ApplicationInfo): String {
        return if (ai.splitSourceDirs.isNullOrEmpty()) getString(R.string.apk) else getString(R.string.split_packages)
    }

    private fun Context.getCategoryString(ai: ApplicationInfo): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MetaUtils.getCategory(ai.category, this)
        } else {
            null
        }
    }

    private fun getSdkRangeString(ai: ApplicationInfo, includeMin: Boolean): String {
        return if (includeMin) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                "${ai.minSdkVersion}..${ai.targetSdkVersion}"
            } else {
                ai.targetSdkVersion.toString()
            }
        } else {
            ai.targetSdkVersion.toString()
        }
    }

    private fun Context.getAppTypeString(ai: ApplicationInfo): String {
        return when {
            (ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0 -> {
                getString(R.string.user)
            }
            else -> {
                getString(R.string.system)
            }
        }
    }

    private fun Context.getStateString(packageInfo: PackageInfo): String {
        return if (packageInfo.isInstalled().not()) {
            getString(R.string.uninstalled)
        } else if (packageInfo.safeApplicationInfo.enabled) {
            getString(R.string.enabled)
        } else {
            getString(R.string.disabled)
        }
    }

    private fun StringBuilder.appendOR(value: CharSequence) {
        if (isNotEmpty()) {
            append(" | ")
        }
        append(value)
    }
}
