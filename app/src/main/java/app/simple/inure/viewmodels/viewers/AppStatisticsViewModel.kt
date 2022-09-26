package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.UsageInterval
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppStatisticsViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private var usageStatsManager: UsageStatsManager = getApplication<Application>()
        .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var networkStatsManager = getApplication<Application>()
        .getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    private val totalUsedChartData: MutableLiveData<Pair<ArrayList<Entry>, ArrayList<String>>> by lazy {
        MutableLiveData<Pair<ArrayList<Entry>, ArrayList<String>>>().also {
            loadStatsData()
        }
    }

    private val totalAppSize: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>().also {
            loadTotalAppSize()
        }
    }

    fun getTotalUsedChartData(): LiveData<Pair<ArrayList<Entry>, ArrayList<String>>> {
        return totalUsedChartData
    }

    private fun loadStatsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                totalUsedChartData.postValue(getUsageEvents())
            }.getOrElse {
                error.postValue(it.stackTraceToString())
                it.printStackTrace()
            }
        }
    }

    private fun getUsageEvents(): Pair<ArrayList<Entry>, ArrayList<String>> {
        val entries: ArrayList<Entry> = ArrayList()
        val dates = arrayListOf<String>()

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
            val packageName = event.packageName

            if (packageInfo.packageName == packageName) {
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
                            if (packageName.equals(event.packageName)) {
                                val time = endTime - startTime + 1
                                entries.add(iteration, Entry(iteration.toFloat(), time.div(1000).div(60).toFloat()))
                                dates.add(iteration, endTime.toDate())

                                iteration++
                            }
                            break
                        }
                    }
                }
            }
        }

        return Pair(entries, dates)
    }

    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = apps

            var size = 0L

            for (i in apps.indices) {
                size += apps[i].sourceDir.getDirectoryLength()
            }

            totalAppSize.postValue(size)
        }
    }

    fun getTotalAppSize(): LiveData<Long> {
        return totalAppSize
    }
}
