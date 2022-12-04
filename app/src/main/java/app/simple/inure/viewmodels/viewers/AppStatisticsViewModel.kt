package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.usage.UsageEvents
import android.content.pm.PackageInfo
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.UsageStatsViewModel
import app.simple.inure.models.AppUsageModel
import app.simple.inure.models.DataUsage
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppStatisticsViewModel(application: Application, private val packageInfo: PackageInfo) : UsageStatsViewModel(application) {

    private val usageData: MutableLiveData<PackageStats> by lazy {
        MutableLiveData<PackageStats>().also {
            loadStatsData()
        }
    }

    fun getUsageData(): LiveData<PackageStats> {
        return usageData
    }

    private fun loadStatsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(getUsageEvents()) {
                    if (this.appUsage?.size?.isNotZero() == true) {
                        usageData.postValue(this)
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
        } else packageStats.mobileData = DataUsage.EMPTY
        if (wifiData.containsKey(uid)) {
            packageStats.wifiData = wifiData[uid]
        } else packageStats.wifiData = DataUsage.EMPTY
    }

    @Suppress("unused")
    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = getInstalledApps()

            var size = 0L

            for (i in apps.indices) {
                size += apps[i].applicationInfo.sourceDir.getDirectoryLength()
            }
        }
    }
}
