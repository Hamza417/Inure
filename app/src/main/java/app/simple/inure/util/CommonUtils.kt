package app.simple.inure.util

import kotlinx.coroutines.flow.MutableStateFlow

object CommonUtils {

    /**
     * Execute a block of code with a boolean scope flag. What it does is set the flag to true
     * before executing the block, and then set it back to false after the block is done.
     */
    inline fun <T> withBooleanScope(scopeFlag: MutableStateFlow<Boolean>, block: () -> T): T {
        scopeFlag.value = true
        return try {
            block()
        } finally {
            scopeFlag.value = false
        }
    }

    /**
     * Execute a block of code with a boolean scope flag. What it does is set the flag to true
     * before executing the block, and then set it back to false after the block is done.
     */
    inline fun <T> withInvertedBooleanScope(scopeFlag: MutableStateFlow<Boolean>, block: () -> T): T {
        scopeFlag.value = false
        return try {
            block()
        } finally {
            scopeFlag.value = true
        }
    }
}
