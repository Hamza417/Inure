package app.simple.inure.preferences

object FormattingPreferences {

    private const val SIZE_TYPE = "size_type"
    private const val DATE_FORMAT = "app_date_format"

    const val COUNT_ALL_LINES = "count_all_lines_in_code_viewers"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSizeType(font: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SIZE_TYPE, font).apply()
    }

    fun getSizeType(): String {
        return SharedPreferences.getSharedPreferences().getString(SIZE_TYPE, "si")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDateFormat(font: String) {
        SharedPreferences.getSharedPreferences().edit().putString(DATE_FORMAT, font).apply()
    }

    fun getDateFormat(): String {
        return SharedPreferences.getSharedPreferences().getString(DATE_FORMAT, "EEE, yyyy MMM dd, hh:mm a")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCountAllLines(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(COUNT_ALL_LINES, value).apply()
    }

    fun isCountingAllLines(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(COUNT_ALL_LINES, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun getLargeStringLimit(): Int {
        return 150000
    }
}
