package app.simple.inure.preferences

object FormattingPreferences {

    private const val sizeType = "size_type"
    private const val dateFormat = "app_date_format"

    const val countAllLines = "count_all_lines_in_code_viewers"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setSizeType(font: String) {
        SharedPreferences.getSharedPreferences().edit().putString(sizeType, font).apply()
    }

    fun getSizeType(): String {
        return SharedPreferences.getSharedPreferences().getString(sizeType, "si")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setDateFormat(font: String) {
        SharedPreferences.getSharedPreferences().edit().putString(dateFormat, font).apply()
    }

    fun getDateFormat(): String {
        return SharedPreferences.getSharedPreferences().getString(dateFormat, "EEE, yyyy MMM dd, hh:mm a")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setCountAllLines(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(countAllLines, value).apply()
    }

    fun isCountingAllLines(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(countAllLines, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun getLargeStringLimit(): Int {
        return 150000
    }
}