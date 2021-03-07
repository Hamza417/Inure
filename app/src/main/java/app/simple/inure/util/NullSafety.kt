package app.simple.inure.util

object NullSafety {
    /**
     * Checks if the given object is not null
     * @return true if object is not null
     */
    fun Any?.isNotNull(): Boolean {
        return this != null
    }

    /**
     * Checks if the given object is null
     * @return true if object is null
     */
    fun Any?.isNull(): Boolean {
        return this == null
    }
}