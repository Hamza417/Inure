package app.simple.inure.preferences

object CrashPreferences {

    private const val CRASH_TIMESTAMP = "crash_timestamp"
    private const val CRASH_MESSAGE = "crash_message"
    private const val CRASH_CAUSE = "crashCause"

    const val CRASH_TIMESTAMP_EMPTY_DEFAULT = -1L

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveCrashLog(value: Long): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putLong(CRASH_TIMESTAMP, value).commit()
    }

    fun getCrashLog(): Long {
        return SharedPreferences.getSharedPreferences().getLong(CRASH_TIMESTAMP, CRASH_TIMESTAMP_EMPTY_DEFAULT)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveMessage(value: String?): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(CRASH_MESSAGE, value).commit()
    }

    fun getMessage(): String? {
        return SharedPreferences.getSharedPreferences().getString(CRASH_MESSAGE, null)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveCause(value: String?): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(CRASH_CAUSE, value).commit()
    }

    fun getCause(): String? {
        return SharedPreferences.getSharedPreferences().getString(CRASH_CAUSE, null)
    }
}
