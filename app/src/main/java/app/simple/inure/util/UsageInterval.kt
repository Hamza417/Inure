package app.simple.inure.util

import app.simple.inure.preferences.StatsPreferences
import java.util.*
import java.util.concurrent.TimeUnit

object UsageInterval {

    const val TODAY = "today"
    const val DAILY = "daily"
    const val WEEKlY = "weekly"
    const val MONTHLY = "monthly"
    const val YEARLY = "yearly"

    fun getTimeInterval(): Pair<Long, Long> {
        return when (StatsPreferences.getInterval()) {
            TODAY -> getTodayInterval()
            DAILY -> getDailyInterval()
            WEEKlY -> getWeeklyInterval()
            MONTHLY -> getMonthlyInterval()
            YEARLY -> getYearlyInterval()
            else -> getWeeklyInterval()
        }
    }

    fun getTimeInterval(interval: String): Pair<Long, Long> {
        return when (interval) {
            TODAY -> getTodayInterval()
            DAILY -> getDailyInterval()
            WEEKlY -> getWeeklyInterval()
            MONTHLY -> getMonthlyInterval()
            YEARLY -> getYearlyInterval()
            else -> getWeeklyInterval()
        }
    }

    private fun getTodayInterval(): Pair<Long, Long> {
        val timeStart: Long = with(Calendar.getInstance()) {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val timeEnd = System.currentTimeMillis()

        return Pair(timeStart, timeEnd)
    }

    private fun getDailyInterval(): Pair<Long, Long> {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
        return Pair(timeStart, timeEnd)
    }

    private fun getWeeklyInterval(): Pair<Long, Long> {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
        return Pair(timeStart, timeEnd)
    }

    private fun getMonthlyInterval(): Pair<Long, Long> {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)
        return Pair(timeStart, timeEnd)
    }

    private fun getYearlyInterval(): Pair<Long, Long> {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS)
        return Pair(timeStart, timeEnd)
    }
}