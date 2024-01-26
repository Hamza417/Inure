package app.simple.inure.constants

object DebloatSortConstants {

    const val AOSP = 1 shl 0
    const val CARRIER = 1 shl 1
    const val GOOGLE = 1 shl 2
    const val MISC = 1 shl 3
    const val OEM = 1 shl 4
    const val PENDING = 1 shl 5
    const val UNLISTED_LIST = 1 shl 6
    const val ALL_LIST = AOSP or CARRIER or GOOGLE or MISC or OEM or PENDING or UNLISTED_LIST

    const val RECOMMENDED = 1 shl 0
    const val ADVANCED = 1 shl 1
    const val EXPERT = 1 shl 2
    const val UNSAFE = 1 shl 3
    const val UNLISTED = 1 shl 4
    const val ALL_REMOVAL = RECOMMENDED or ADVANCED or EXPERT or UNSAFE or UNLISTED

    const val DISABLED = 1 shl 0
    const val ENABLED = 1 shl 1
    const val UNINSTALLED = 1 shl 2
    const val ALL_STATE = DISABLED or ENABLED or UNINSTALLED

}