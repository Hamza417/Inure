package app.simple.inure.preferences

object CrashPreferences {

    private const val crashTimestamp = "crash_timestamp"
    private const val crashMessage = "crash_message"
    private const val crashCause = "crashCause"

    const val crashTimestampEmptyDefault = -1L

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveCrashLog(value: Long): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putLong(crashTimestamp, value).commit()
    }

    fun getCrashLog(): Long {
        return SharedPreferences.getSharedPreferences().getLong(crashTimestamp, crashTimestampEmptyDefault)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveMessage(value: String?): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(crashMessage, value).commit()
    }

    fun getMessage(): String? {
        return SharedPreferences.getSharedPreferences().getString(crashMessage, null)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveCause(value: String?): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(crashCause, value).commit()
    }

    fun getCause(): String? {
        return SharedPreferences.getSharedPreferences().getString(crashCause, null)
    }
}