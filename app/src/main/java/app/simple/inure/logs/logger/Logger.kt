package app.simple.inure.logs.logger

import android.util.Log
import kotlin.reflect.KClass

object Logger {
    private const val DEBUG = 1
    private const val ERROR = 2
    private const val INFO = 3
    private const val VERBOSE = 4
    private const val WARNING = 5
    private const val WTF = 6

    private var sTag = "Logger"

    fun init(tag: String) {
        sTag = tag
    }

    private fun KClass<*>.name(): String = simpleName ?: "N/A"

    fun debug(type: KClass<*>, msg: String) {
        debug(type.name(), msg)
    }

    fun error(type: KClass<*>, msg: String, e: Throwable? = null) {
        error(type.name(), msg, e)
    }

    fun info(type: KClass<*>, msg: String) {
        info(type.name(), msg)
    }

    fun verbose(type: KClass<*>, msg: String) {
        verbose(type.name(), msg)
    }

    fun warning(type: KClass<*>, msg: String) {
        warning(type.name(), msg)
    }

    fun wtf(type: KClass<*>, msg: String) {
        wtf(type.name(), msg)
    }

    fun debug(tag: String, msg: String) {
        log(DEBUG, "[$tag] $msg")
    }

    fun error(tag: String, msg: String, e: Throwable? = null) {
        log(ERROR, "[$tag] $msg", e)
    }

    fun info(tag: String, msg: String) {
        log(INFO, "[$tag] $msg")
    }

    fun verbose(tag: String, msg: String) {
        log(VERBOSE, "[$tag] $msg")
    }

    fun warning(tag: String, msg: String) {
        log(WARNING, "[$tag] $msg")
    }

    fun wtf(tag: String, msg: String) {
        log(WTF, "[$tag] $msg")
    }

    private fun log(type: Int, msg: String, e: Throwable? = null) {
        when (type) {
            DEBUG -> Log.d(sTag, msg)
            ERROR -> Log.e(sTag, msg, e)
            INFO -> Log.i(sTag, msg)
            VERBOSE -> Log.v(sTag, msg)
            WARNING -> Log.w(sTag, msg)
            WTF -> Log.wtf(sTag, msg)
        }
    }
}