package app.simple.inure.logs.logcat

interface Filter {
    fun apply(log: Log): Boolean
}