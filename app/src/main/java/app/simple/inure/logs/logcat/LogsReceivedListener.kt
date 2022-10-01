package app.simple.inure.logs.logcat

import androidx.annotation.MainThread

interface LogsReceivedListener {
    @MainThread
    fun onReceivedLogs(logs: List<Log>)
}