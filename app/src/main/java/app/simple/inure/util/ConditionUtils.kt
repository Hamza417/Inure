package app.simple.inure.util

@Suppress("unused")
object ConditionUtils {
    /**
     * Quickly performs a null safety check
     * of a potential null object that has
     * no way to initialize but to throw an
     * exception. This approach is unsafe
     * and should not be used with conditional
     * statements
     *
     * Requires casting to the original object
     *
     * @throws UninitializedPropertyAccessException
     * @return [Any]
     */
    fun Any?.asNotNull(): Any {
        return this ?: throw UninitializedPropertyAccessException()
    }

    /**
     * Check if an object is null
     *
     * @return true if null
     */
    fun Any?.isNull(): Boolean {
        return this == null
    }

    /**
     * Check if an object is null
     *
     * @return true if not null
     */
    fun Any?.isNotNull(): Boolean {
        return this != null
    }

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

    /**
     * Check is a number is equal to another number
     *
     * @return [Boolean]
     */
    fun Number.isEqualTo(number: Number): Boolean {
        return this == number
    }

    /**
     * Inverts the current boolean
     *
     * @return [Boolean]
     */
    fun Boolean.invert(): Boolean {
        return !this
    }
}
