package app.simple.inure.util

import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt

object NumberUtils {
    /**
     * @param timeValue strictly takes the long value of milliseconds and
     * formats them accordingly, if the [timeValue] is less than hour, it will
     * automatically be formatted as mm:ss format and if larger than that
     * it will be formatted as hh:mm:ss
     */
    fun getFormattedTime(timeValue: Long): String {
        // mm:ss
        return if (timeValue < 3600000) {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes("$timeValue".toLong()) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds("$timeValue".toLong()) % TimeUnit.MINUTES.toSeconds(1))
        } else {
            //hh:mm:ss
            String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timeValue),
                TimeUnit.MILLISECONDS.toMinutes(timeValue) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeValue)),
                TimeUnit.MILLISECONDS.toSeconds(timeValue) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeValue)))
        }
    }

    /**
     * Rounds the decimal places to the specified places
     * @param places is the number of significant digits required
     * @param number is the main value, must be a double or atleast contains some fractional values
     */
    fun round(number: Double, places: Int): Double {
        return try {
            var value = number
            require(places >= 0)
            val factor = 10.0.pow(places.toDouble()).toLong()
            value *= factor
            val tmp = value.roundToInt()
            tmp.toDouble() / factor
        } catch (e: IllegalArgumentException) {
            Double.NaN
        }
    }
}