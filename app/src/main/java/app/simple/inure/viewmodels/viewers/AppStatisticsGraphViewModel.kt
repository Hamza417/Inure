package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.usage.UsageEvents
import android.content.pm.PackageInfo
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.UsageStatsViewModel
import app.simple.inure.models.AppUsageModel
import app.simple.inure.models.DataUsage
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.UsageInterval
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

class AppStatisticsGraphViewModel(application: Application, private val packageInfo: PackageInfo) : UsageStatsViewModel(application) {

    private val packageStats: MutableLiveData<PackageStats> by lazy {
        MutableLiveData<PackageStats>().also {
            loadStatsData()
        }
    }

    private val barChartData: MutableLiveData<ArrayList<BarEntry>> by lazy {
        MutableLiveData<ArrayList<BarEntry>>()
    }

    private val pieChartData: MutableLiveData<ArrayList<PieEntry>> by lazy {
        MutableLiveData<ArrayList<PieEntry>>()
    }

    fun getPackageStats(): LiveData<PackageStats> {
        return packageStats
    }

    fun getChartData(): LiveData<ArrayList<BarEntry>> {
        return barChartData
    }

    fun getPieChartData(): LiveData<ArrayList<PieEntry>> {
        return pieChartData
    }

    private fun loadStatsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(getUsageEvents()) {
                    if (this.appUsage?.size?.isNotZero() == true) {
                        packageStats.postValue(this)
                        loadPieChartData(this)
                        loadBarChartData(this)
                    } else {
                        warning.postValue(getString(R.string.usage_data_does_not_exist_for_this_app))
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getUsageEvents(): PackageStats {
        val packageStats = PackageStats()
        packageStats.packageInfo = packageInfo
        packageStats.appUsage = arrayListOf()

        val interval = UsageInterval.getTimeInterval()
        val events: UsageEvents = usageStatsManager.queryEvents(interval.startTime, interval.endTime)
        val event = UsageEvents.Event()

        var startTime: Long
        var endTime: Long
        var skipNew = false
        var iteration = 0

        while (events.hasNextEvent()) {
            if (!skipNew) events.getNextEvent(event)

            var eventTime = event.timeStamp

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) { // App is visible (foreground)
                startTime = eventTime

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    eventTime = event.timeStamp

                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        skipNew = true
                        break
                    } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        endTime = eventTime
                        skipNew = false
                        if (packageInfo.packageName.equals(event.packageName)) {
                            val time = endTime - startTime + 1
                            packageStats.appUsage?.add(iteration, AppUsageModel(startTime, time, endTime))
                            packageStats.launchCount = iteration.plus(1)
                            packageStats.totalTimeUsed += time
                            packageStats.lastUsageTime = endTime

                            iteration++
                        }
                        break
                    }
                }
            }
        }

        packageStats.appUsage?.reverse()
        getDataUsage(packageStats)

        return packageStats
    }

    private fun getDataUsage(packageStats: PackageStats) {
        var mobileData = SparseArrayCompat<DataUsage>()
        var wifiData = SparseArrayCompat<DataUsage>()

        kotlin.runCatching {
            mobileData = getMobileData(StatisticsPreferences.getInterval())
        }
        kotlin.runCatching {
            wifiData = getWifiData(StatisticsPreferences.getInterval())
        }

        val uid: Int = packageStats.packageInfo?.applicationInfo?.uid!!

        if (mobileData.containsKey(uid)) {
            packageStats.mobileData = mobileData[uid]
        } else {
            packageStats.mobileData = DataUsage.EMPTY
        }

        if (wifiData.containsKey(uid)) {
            packageStats.wifiData = wifiData[uid]
        } else {
            packageStats.wifiData = DataUsage.EMPTY
        }
    }

    private fun loadBarChartData(packageStats: PackageStats) {
        viewModelScope.launch(Dispatchers.Default) {
            val barEntries = arrayListOf(
                    BarEntry(0f, 0f, getDayString(LocalDate.now())),
                    BarEntry(1f, 0f, getDayString(LocalDate.now().minusDays(1))),
                    BarEntry(2f, 0f, getDayString(LocalDate.now().minusDays(2))),
                    BarEntry(3f, 0f, getDayString(LocalDate.now().minusDays(3))),
                    BarEntry(4f, 0f, getDayString(LocalDate.now().minusDays(4))),
                    BarEntry(5f, 0f, getDayString(LocalDate.now().minusDays(5))),
                    BarEntry(6f, 0f, getDayString(LocalDate.now().minusDays(6)))
            )

            packageStats.appUsage?.forEach { it ->
                val number = it.date.getNumberOfDaysBetweenTwoDates()

                try {
                    barEntries[number].y += it.startTime
                    barEntries[number].data = getDayString(it.date.toLocalDate())
                } catch (e: IndexOutOfBoundsException) {
                    try {
                        barEntries.add(number, BarEntry(number.toFloat(), it.startTime.toFloat(), getDayString(it.date.toLocalDate())))
                    } catch (e: IndexOutOfBoundsException) {
                        /**
                         * The number of days between the first and last usage is greater than 7.
                         */
                        barEntries.add(BarEntry(number.toFloat(), it.startTime.toFloat(), getDayString(it.date.toLocalDate())))
                    }
                }
            }

            // Flip values of bar entries
            for (index in 0 until barEntries.size) {
                // Flip x value of bar entry
                barEntries[index].x = (barEntries.size.minus(1) - index).toFloat()

                // Flip data of bar entry
                // barEntries[index].data = copy[barEntries[index].x.toInt()].data
            }

            // Replace empty bar entries with empty bar entry
            for (index in 0 until barEntries.size) {
                if (barEntries[index].y == 0f) {
                    barEntries[index] = BarEntry(barEntries[index].x, 0f, getString(R.string.not_available))
                }
            }

            barChartData.postValue(barEntries)
        }
    }

    private fun loadPieChartData(packageStats: PackageStats) {
        viewModelScope.launch(Dispatchers.Default) {
            val pieEntries = arrayListOf(
                    PieEntry(0f, getDayString(LocalDate.now())),
                    PieEntry(0f, getDayString(LocalDate.now().minusDays(1))),
                    PieEntry(0f, getDayString(LocalDate.now().minusDays(2))),
                    PieEntry(0f, getDayString(LocalDate.now().minusDays(3))),
                    PieEntry(0f, getDayString(LocalDate.now().minusDays(4))),
                    PieEntry(0f, getDayString(LocalDate.now().minusDays(5))),
                    PieEntry(0f, getDayString(LocalDate.now().minusDays(6)))
            )

            packageStats.appUsage?.forEach {
                val numberOfDays = it.date.getNumberOfDaysBetweenTwoDates()

                try {
                    val pieEntry = PieEntry(pieEntries[numberOfDays].value + it.startTime, getDayString(it.date.toLocalDate()))
                    pieEntries.remove(pieEntries[numberOfDays])
                    pieEntries.add(numberOfDays, pieEntry)
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    try {
                        pieEntries.add(numberOfDays, PieEntry(it.startTime.toFloat(), getDayString(it.date.toLocalDate())))
                    } catch (e: IndexOutOfBoundsException) {
                        /**
                         * The numberOfDays is greater than the size of the pieEntries list.
                         * This means that the pieEntries list needs to be expanded.
                         */
                        pieEntries.add(PieEntry(it.startTime.toFloat(), getDayString(it.date.toLocalDate())))
                    }
                }
            }

            // Replace empty pie entries with empty pie entry
            for (index in 0 until pieEntries.size) {
                if (pieEntries[index].value == 0f) {
                    pieEntries[index] = PieEntry(0f, "")
                }
            }

            pieChartData.postValue(pieEntries)
        }
    }

    //    private fun calculateDailyAverage(pieEntries: List<PieEntry>) {
    //        var tally = 0
    //        var total = 0L
    //
    //        for (i in pieEntries.indices) {
    //            if (pieEntries[i].value.isNotZero()) {
    //                tally++
    //                total += pieEntries[i].value.toLong()
    //            }
    //        }
    //
    //        val average = total / pieEntries.size
    //
    //        dailyAverage.postValue(average)
    //    }

    private fun getDayString(date: LocalDate): String? {
        return date.dayOfWeek.getDisplayName(TextStyle.SHORT, LocaleUtils.getAppLocale())
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun Long.getNumberOfDaysBetweenTwoDates(): Int {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault()).toLocalDate()
            .until(LocalDate.now(), ChronoUnit.DAYS).toInt()
    }

    @Suppress("unused")
    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = getInstalledApps()

            var size = 0L

            for (i in apps.indices) {
                size += apps[i].safeApplicationInfo.sourceDir.getDirectoryLength()
            }
        }
    }
}
