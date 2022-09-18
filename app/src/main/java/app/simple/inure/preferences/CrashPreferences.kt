package app.simple.inure.preferences

object CrashPreferences {

    const val crashLog = "crash_log"

    // ---------------------------------------------------------------------------------------------------------- //

    fun saveCrashLog(value: String?) {
        SharedPreferences.getSharedPreferences().edit().putString(crashLog, value).apply()
    }

    fun getCrashLog(): String? {
        return SharedPreferences.getSharedPreferences()
            .getString(crashLog, null)
    }
}