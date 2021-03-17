package app.simple.inure.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(date: Long): String {
        val sdf = SimpleDateFormat("EEE, yyyy MMM dd, hh:mm a", Locale.getDefault())
        return sdf.format(Date(date))
    }
}