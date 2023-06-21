package app.simple.inure.constants

object SortConstant {
    const val SYSTEM = "system"
    const val USER = "user"
    const val BOTH = "both"

    const val ASCENDING = "ascending"
    const val DESCENDING = "descending"

    const val DISABLED = 0x0001
    const val ENABLED = 0x0002
    const val APK = 0x0004
    const val SPLIT = 0x0008
    const val COMBINE_FLAGS = 0x0010
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
}