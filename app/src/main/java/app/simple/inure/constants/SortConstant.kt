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
}