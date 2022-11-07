package app.simple.inure.util

import android.app.usage.UsageStatsManager
import androidx.annotation.IntDef
import app.simple.inure.preferences.StatisticsPreferences
import java.util.*
import java.util.concurrent.TimeUnit

object UsageInterval {

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
    @IntDef(value = [DAILY, WEEKlY, MONTHLY, YEARLY])
    annotation class IntervalType

    @Deprecated("Not natively supported by Android")
    private const val TODAY = -1
    const val DAILY = UsageStatsManager.INTERVAL_DAILY
    const val WEEKlY = UsageStatsManager.INTERVAL_WEEKLY
    const val MONTHLY = UsageStatsManager.INTERVAL_MONTHLY
    const val YEARLY = UsageStatsManager.INTERVAL_YEARLY

    fun getTimeInterval(): UsageInterval {
        @Suppress("DEPRECATION")
        return when (StatisticsPreferences.getInterval()) {
            TODAY -> getTodayInterval()
            DAILY -> getDailyInterval()
            WEEKlY -> getWeeklyInterval()
            MONTHLY -> getMonthlyInterval()
            YEARLY -> getYearlyInterval()
            else -> getWeeklyInterval()
        }
    }

    fun getTimeInterval(interval: Int): UsageInterval {
        @Suppress("DEPRECATION")
        return when (interval) {
            TODAY -> getTodayInterval()
            DAILY -> getDailyInterval()
            WEEKlY -> getWeeklyInterval()
            MONTHLY -> getMonthlyInterval()
            YEARLY -> getYearlyInterval()
            else -> getWeeklyInterval()
        }
    }

    private fun getTodayInterval(): UsageInterval {
        val timeStart: Long = with(Calendar.getInstance()) {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val timeEnd = System.currentTimeMillis()

        return UsageInterval(timeStart, timeEnd)
    }

    private fun getDailyInterval(): UsageInterval {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
        return UsageInterval(timeStart, timeEnd)
    }

    private fun getWeeklyInterval(): UsageInterval {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
        return UsageInterval(timeStart, timeEnd)
    }

    private fun getMonthlyInterval(): UsageInterval {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)
        return UsageInterval(timeStart, timeEnd)
    }

    private fun getYearlyInterval(): UsageInterval {
        val timeEnd = System.currentTimeMillis()
        val timeStart: Long = timeEnd - TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS)
        return UsageInterval(timeStart, timeEnd)
    }

    class UsageInterval(val startTime: Long, val endTime: Long)
}