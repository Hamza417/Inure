package app.simple.inure.util

import android.content.Context

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

    /**
     * Check if context is null then throw
     * exception
     *
     * @throws NullPointerException
     * @return self
     */
    fun Context?.isNull(): Context {
        return this ?: throw NullPointerException("Initialize context first")
    }
}