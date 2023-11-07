package app.simple.inure.constants

import android.os.Build
import androidx.annotation.RequiresApi

object SortConstant {
    const val SYSTEM = "system"
    const val USER = "user"
    const val BOTH = "both"

    const val ASCENDING = "ascending"
    const val DESCENDING = "descending"

    const val DISABLED = 1 shl 1
    const val ENABLED = 1 shl 2
    const val APK = 1 shl 3
    const val SPLIT = 1 shl 4
    const val UNINSTALLED = 1 shl 5
    const val FOSS = 1 shl 6
    const val COMBINE_FLAGS = 1 shl 7
    const val ALL = DISABLED or ENABLED or APK or SPLIT

    const val CATEGORY_UNSPECIFIED = 1L shl 1
    const val CATEGORY_GAME = 1L shl 2
    const val CATEGORY_AUDIO = 1L shl 3
    const val CATEGORY_VIDEO = 1L shl 4
    const val CATEGORY_IMAGE = 1L shl 5
    const val CATEGORY_SOCIAL = 1L shl 6
    const val CATEGORY_NEWS = 1L shl 7
    const val CATEGORY_MAPS = 1L shl 8
    const val CATEGORY_PRODUCTIVITY = 1L shl 9

    @RequiresApi(Build.VERSION_CODES.S)
    const val CATEGORY_ACCESSIBILITY = 1L shl 10

    var ALL_CATEGORIES =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CATEGORY_UNSPECIFIED or
                    CATEGORY_GAME or
                    CATEGORY_AUDIO or
                    CATEGORY_VIDEO or
                    CATEGORY_IMAGE or
                    CATEGORY_SOCIAL or
                    CATEGORY_NEWS or
                    CATEGORY_MAPS or
                    CATEGORY_PRODUCTIVITY or
                    CATEGORY_ACCESSIBILITY
        } else {
            CATEGORY_UNSPECIFIED or
                    CATEGORY_GAME or
                    CATEGORY_AUDIO or
                    CATEGORY_VIDEO or
                    CATEGORY_IMAGE or
                    CATEGORY_SOCIAL or
                    CATEGORY_NEWS or
                    CATEGORY_MAPS or
                    CATEGORY_PRODUCTIVITY
        }

    const val OPTIMIZED = 1
    const val NOT_OPTIMIZED = 2
    const val ALL_OPTIMIZATION_STATES = OPTIMIZED or NOT_OPTIMIZED

    const val BATCH_SELECTED = 1
    const val BATCH_NOT_SELECTED = 2
    const val BATCH_ENABLED = 4
    const val BATCH_DISABLED = 8
    const val ALL_BATCH_STATES = BATCH_SELECTED or BATCH_NOT_SELECTED or BATCH_ENABLED or BATCH_DISABLED

    const val BOOT_ENABLED = 1
    const val BOOT_DISABLED = 2
    const val ALL_BOOT_STATES = BOOT_ENABLED or BOOT_DISABLED

    const val APKS_APK = 1
    const val APKS_APKS = 2
    const val APKS_APKM = 4
    const val APKS_XAPK = 8
    const val APKS_HIDDEN = 16
    const val ALL_APKS = APKS_APK or APKS_APKS or APKS_APKM or APKS_XAPK

    const val INFO_TYPE = 1
    const val INFO_SIZE = 2
    const val INFO_STATE = 4
    const val INFO_CATEGORY = 8
    const val INFO_PACKAGE_TYPE = 16
    const val INFO_MIN_SDK = 32
    const val INFO_TARGET_SDK = 64
    const val INFO_INSTALL_DATE = 128
    const val INFO_UPDATE_DATE = 256
    const val INFO_VERSION = 512
    const val INFO_DEFAULT = INFO_TYPE or INFO_SIZE or INFO_STATE or INFO_CATEGORY or INFO_PACKAGE_TYPE
}