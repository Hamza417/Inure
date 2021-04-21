package app.simple.inure.util

import java.util.*

object StringUtils {
    fun String.capitalizeFirstLetter(): String {
        return try {
            this.substring(0, 1).toUpperCase(Locale.ROOT) + this.substring(1)
        } catch (e: IndexOutOfBoundsException) {
            this
        }
    }
}