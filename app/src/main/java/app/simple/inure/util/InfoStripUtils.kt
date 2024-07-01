package app.simple.inure.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.getPackageSize
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.preferences.DevelopmentPreferences
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
                if ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appendOR(getString(R.string.user))
                } else {
                    appendOR(getString(R.string.system))
                }
            }

            // Size
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_SIZE)) {
                if (DevelopmentPreferences.get(DevelopmentPreferences.showCompleteAppSize)) {
                    if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        with(packageInfo.getPackageSize(this@getAppInfo)) {
                            appendOR((cacheSize +
                                    dataSize +
                                    codeSize +
                                    externalCacheSize +
                                    externalDataSize +
                                    externalMediaSize +
                                    externalObbSize +
                                    externalCodeSize +
                                    packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                        }
                    } else {
                        with(packageInfo.getPackageSize(this@getAppInfo)) {
                            appendOR((cacheSize +
                                    dataSize +
                                    codeSize +
                                    externalCacheSize +
                                    externalDataSize +
                                    externalMediaSize +
                                    externalObbSize +
                                    externalCodeSize +
                                    packageInfo.applicationInfo.sourceDir.toLength() +
                                    (packageInfo.applicationInfo.splitSourceDirs ?: arrayOf()).getDirectorySize()).toSize())
                        }
                    }
                } else {
                    if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        appendOR(packageInfo.applicationInfo.sourceDir.toSize())
                    } else {
                        appendOR((packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize() +
                                packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                    }
                }
            }

            // State
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_STATE)) {
                if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                    appendOR(getString(R.string.uninstalled))
                } else {
                    if (packageInfo.applicationInfo.enabled) {
                        appendOR(getString(R.string.enabled))
                    } else {
                        appendOR(getString(R.string.disabled))
                    }
                }
            }

            // Category
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_CATEGORY)) {
                    appendOR(MetaUtils.getCategory(packageInfo.applicationInfo.category, this@getAppInfo))
                }
            }

            // Target SDK
            if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_PACKAGE_TYPE)) {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    appendOR(getString(R.string.apk))
                } else {
                    appendOR(getString(R.string.split_packages))
                }
            }

            // Min SDK
            when {
                FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_MIN_SDK or SortConstant.INFO_TARGET_SDK) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appendOR(packageInfo.applicationInfo.minSdkVersion.toString())
                        append("..")
                        append(packageInfo.applicationInfo.targetSdkVersion)
                    } else {
                        append(packageInfo.applicationInfo.targetSdkVersion)
                    }
                }
                FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_MIN_SDK) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appendOR(packageInfo.applicationInfo.minSdkVersion.toString())
                    }
                }
                FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), SortConstant.INFO_TARGET_SDK) -> {
                    appendOR(packageInfo.applicationInfo.targetSdkVersion.toString())
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

    fun TypeFaceTextView.setAppInfoNoFlag(packageInfo: PackageInfo) {
        buildString {
            // Version
            append(packageInfo.versionName)

            // Type
            if ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                appendOR(context.getString(R.string.user))
            } else {
                appendOR(context.getString(R.string.system))
            }

            // Size
            if (DevelopmentPreferences.get(DevelopmentPreferences.showCompleteAppSize)) {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    with(packageInfo.getPackageSize(context)) {
                        appendOR((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                    }
                } else {
                    with(packageInfo.getPackageSize(context)) {
                        appendOR((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength() +
                                packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize()).toSize())
                    }
                }
            } else {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    appendOR(packageInfo.applicationInfo.sourceDir.toSize())
                } else {
                    appendOR((packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize() +
                            packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                }
            }

            // State
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                appendOR(context.getString(R.string.uninstalled))
            } else {
                if (packageInfo.applicationInfo.enabled) {
                    appendOR(context.getString(R.string.enabled))
                } else {
                    appendOR(context.getString(R.string.disabled))
                }
            }

            // Category
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appendOR(MetaUtils.getCategory(packageInfo.applicationInfo.category, context))
            }

            // Target SDK
            if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                appendOR(context.getString(R.string.apk))
            } else {
                appendOR(context.getString(R.string.split_packages))
            }

            // Min SDK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appendOR(packageInfo.applicationInfo.minSdkVersion.toString())
                append("..")
                append(packageInfo.applicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.applicationInfo.targetSdkVersion)
            }

            text = toString()
        }
    }

    fun TypeFaceTextView.setUninstalledAppInfo(packageInfo: PackageInfo) {
        buildString {
            append(packageInfo.versionName)
            append(" | ")

            if ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                append(context.getString(R.string.user))
            } else {
                append(context.getString(R.string.system))
            }

            append(" | ")
            if (DevelopmentPreferences.get(DevelopmentPreferences.showCompleteAppSize)) {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    with(packageInfo.getPackageSize(context)) {
                        append((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                    }
                } else {
                    with(packageInfo.getPackageSize(context)) {
                        append((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength() +
                                packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize()).toSize())
                    }
                }
            } else {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    append(packageInfo.applicationInfo.sourceDir.toSize())
                } else {
                    append((packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize() +
                            packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                append(" | ")
                append(MetaUtils.getCategory(packageInfo.applicationInfo.category, context))
            }

            if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(" | ")
                append(context.getString(R.string.apk))
            } else {
                append(" | ")
                append(context.getString(R.string.split_packages))
            }

            append(" | ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(packageInfo.applicationInfo.minSdkVersion)
                append("..")
                append(packageInfo.applicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.applicationInfo.targetSdkVersion)
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
            if (DevelopmentPreferences.get(DevelopmentPreferences.showCompleteAppSize)) {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    with(packageInfo.getPackageSize(context)) {
                        append((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                    }
                } else {
                    with(packageInfo.getPackageSize(context)) {
                        append((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength() +
                                packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize()).toSize())
                    }
                }
            } else {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    append(packageInfo.applicationInfo.sourceDir.toSize())
                } else {
                    append((packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize() +
                            packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                }
            }
            append(" | ")
            if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(context.getString(R.string.apk))
            } else {
                append(context.getString(R.string.split_packages))
            }

            append(" | ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(packageInfo.applicationInfo.minSdkVersion)
                append("..")
                append(packageInfo.applicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.applicationInfo.targetSdkVersion)
            }

        }.also { text = it }
    }

    fun TypeFaceTextView.setRecentlyUpdatedInfo(packageInfo: PackageInfo) {
        buildString {
            append(packageInfo.lastUpdateTime.toDate())
            append(" | ")
            //            append(packageInfo.versionName)
            //            append(" | ")
            if (DevelopmentPreferences.get(DevelopmentPreferences.showCompleteAppSize)) {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    with(packageInfo.getPackageSize(context)) {
                        append((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                    }
                } else {
                    with(packageInfo.getPackageSize(context)) {
                        append((cacheSize +
                                dataSize +
                                codeSize +
                                externalCacheSize +
                                externalDataSize +
                                externalMediaSize +
                                externalObbSize +
                                externalCodeSize +
                                packageInfo.applicationInfo.sourceDir.toLength() +
                                packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize()).toSize())
                    }
                }
            } else {
                if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    append(packageInfo.applicationInfo.sourceDir.toSize())
                } else {
                    append((packageInfo.applicationInfo.splitSourceDirs!!.getDirectorySize() +
                            packageInfo.applicationInfo.sourceDir.toLength()).toSize())
                }
            }
            append(" | ")
            if (packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                append(context.getString(R.string.apk))
            } else {
                append(context.getString(R.string.split_packages))
            }

            append(" | ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                append(packageInfo.applicationInfo.minSdkVersion)
                append("..")
                append(packageInfo.applicationInfo.targetSdkVersion)
            } else {
                append(packageInfo.applicationInfo.targetSdkVersion)
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
