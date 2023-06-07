package app.simple.inure.util

import android.util.Log

object Logger {
    fun postDebugLog(message: String, tag: String = "Inure") {
        Log.d(tag, message)
    }

    fun postErrorLog(message: String, tag: String = "Inure") {
        Log.e(tag, message)
    }

    fun postInfoLog(message: String, tag: String = "Inure") {
        Log.i(tag, message)
    }

    fun postVerboseLog(message: String, tag: String = "Inure") {
        Log.v(tag, message)
    }

    fun postWarningLog(message: String, tag: String = "Inure") {
        Log.w(tag, message)
    }

    fun postWTFLog(message: String, tag: String = "Inure") {
        Log.wtf(tag, message)
    }
}