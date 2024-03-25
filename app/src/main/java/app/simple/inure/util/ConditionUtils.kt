package app.simple.inure.util

@Suppress("unused")
object ConditionUtils {
    /**
     * Checks if a number is 0
     *
     * @return [Boolean]
     */
    fun Number.isZero(): Boolean {
        return this == 0
    }

    /**
     * Checks if a number is not 0
     *
     * @return [Boolean]
     */
    fun Number.isNotZero(): Boolean {
        return this != 0
    }

    fun Float.isNotZero(): Boolean {
        return this != 0F
    }

    /**
     * Check is a number is equal to another number
     *
     * @return [Boolean]
     */
    fun Number.isEqualTo(number: Number): Boolean {
        return this == number
    }

    /**
     * returns the inverse of the boolean
     *
     * @return [Boolean]
     */
    fun Boolean.invert(): Boolean {
        return !this
    }
}
