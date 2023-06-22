package app.simple.inure.constants

object SortConstant {
    const val SYSTEM = "system"
    const val USER = "user"
    const val BOTH = "both"

    const val ASCENDING = "ascending"
    const val DESCENDING = "descending"

    const val DISABLED = 1
    const val ENABLED = 2
    const val APK = 4
    const val SPLIT = 8
    const val UNINSTALLED = 16
    const val COMBINE_FLAGS = 32
    const val ALL = DISABLED or ENABLED or APK or SPLIT

    const val CATEGORY_UNSPECIFIED = 1
    const val CATEGORY_GAME = 2
    const val CATEGORY_AUDIO = 4
    const val CATEGORY_VIDEO = 8
    const val CATEGORY_IMAGE = 16
    const val CATEGORY_SOCIAL = 32
    const val CATEGORY_NEWS = 64
    const val CATEGORY_MAPS = 128
    const val CATEGORY_PRODUCTIVITY = 256
    const val CATEGORY_ACCESSIBILITY = 512

    const val ALL_CATEGORIES =
        CATEGORY_GAME or
                CATEGORY_AUDIO or
                CATEGORY_VIDEO or
                CATEGORY_IMAGE or
                CATEGORY_SOCIAL or
                CATEGORY_NEWS or
                CATEGORY_MAPS or
                CATEGORY_PRODUCTIVITY or
                CATEGORY_ACCESSIBILITY

    const val OPTIMIZED = 1
    const val NOT_OPTIMIZED = 2
    const val ALL_OPTIMIZATION_STATES = OPTIMIZED or NOT_OPTIMIZED

    const val BATCH_SELECTED = 1
    const val BATCH_NOT_SELECTED = 2
    const val BATCH_ENABLED = 4
    const val BATCH_DISABLED = 8
    const val ALL_BATCH_STATES = BATCH_SELECTED or BATCH_NOT_SELECTED or BATCH_ENABLED or BATCH_DISABLED
}