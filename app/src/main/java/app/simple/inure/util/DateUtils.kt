package app.simple.inure.util

import app.simple.inure.preferences.FormattingPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun Long.toDate(): String {
        val sdf = SimpleDateFormat(FormattingPreferences.getDateFormat(), Locale.getDefault())
        return sdf.format(Date(this))
    }

    fun Long.toDate(pattern: String): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(this))
    }

    fun String.toLong(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.parse(this)!!.time
    }

    fun formatDate(date: Long): String {
        val sdf = SimpleDateFormat("EEE, yyyy MMM dd, hh:mm a", Locale.getDefault())
        return sdf.format(Date(date))
    }

    fun formatDate(date: Long, pattern: String): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(date))
    }

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("EEE, yyyy MMM dd, hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}