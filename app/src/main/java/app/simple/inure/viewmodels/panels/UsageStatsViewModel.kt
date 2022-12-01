package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.models.DataUsage
import app.simple.inure.models.PackageStats
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.popups.usagestats.PopupUsageStatsEngine
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.SortUsageStats.sortStats
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class UsageStatsViewModel(application: Application) : app.simple.inure.extensions.viewmodels.UsageStatsViewModel(application) {

    val usageData: MutableLiveData<ArrayList<PackageStats>> by lazy {
        MutableLiveData<ArrayList<PackageStats>>().also {
            loadAppStats()
        }
    }

    val progress = MutableLiveData<Int>()
    val max = MutableLiveData<Int>()

    fun loadAppStats() {
        viewModelScope.launch(Dispatchers.Default) {
            var list = when (StatisticsPreferences.getEngine()) {
                PopupUsageStatsEngine.INURE -> {
                    getUsageStats()
                }
                PopupUsageStatsEngine.ANDROID -> {
                    getUsageEvents()
                }
                else -> {
                    StatisticsPreferences.setEngine(PopupUsageStatsEngine.INURE)
                    throw java.lang.IllegalStateException("Unknown engine type detected by Inure" +
                                                                  " - app will reset engine preferences")
                }
            }

            when (StatisticsPreferences.getAppsCategory()) {
                PopupAppsCategory.SYSTEM -> {
                    list = list.stream().filter { p ->
                        p.packageInfo!!.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageStats>
                }
                PopupAppsCategory.USER -> {
                    list = list.stream().filter { p ->
                        p.packageInfo!!.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageStats>
                }
            }

            for (app in list) {
                app.packageInfo!!.applicationInfo.name = getApplicationName(applicationContext(), app.packageInfo!!.applicationInfo)
            }

            if (StatisticsPreferences.areUnusedAppHidden()) {
                list = list.filter {
                    it.totalTimeUsed != 0L
                } as ArrayList<PackageStats>
            }

            list.sortStats()

            usageData.postValue(list)
        }
    }

    fun sortUsageData() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = usageData.value

            list?.sortStats()?.also {
                usageData.postValue(list)
            }
        }
    }

    private fun getUsageStats(): ArrayList<PackageStats> {
        val list = arrayListOf<PackageStats>()
        val stats: MutableList<UsageStats> = with(UsageInterval.getTimeInterval()) {
            usageStatsManager.queryUsageStats(StatisticsPreferences.getInterval(), startTime, endTime)
        }

        var apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        }

        when (StatisticsPreferences.getAppsCategory()) {
            PopupAppsCategory.SYSTEM -> {
                apps = apps.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
            PopupAppsCategory.USER -> {
                apps = apps.stream().filter { p ->
                    p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>
            }
        }

        var mobileData = SparseArrayCompat<DataUsage>()
        var wifiData = SparseArrayCompat<DataUsage>()

        kotlin.runCatching {
            mobileData = getMobileData(StatisticsPreferences.getInterval())
        }
        kotlin.runCatching {
            wifiData = getWifiData(StatisticsPreferences.getInterval())
        }

        for (app in apps) {
            kotlin.runCatching {
                val packageStats = PackageStats()

                packageStats.packageInfo = app

                val p0 = stats.stream().filter {
                    it.packageName == app.packageName
                }.collect(Collectors.toList())

                for (usageStats in p0) {
                    packageStats.totalTimeUsed += usageStats.totalTimeInForeground
                }

                val uid: Int = packageStats.packageInfo?.applicationInfo?.uid!!

                if (mobileData.containsKey(uid)) {
                    packageStats.mobileData = mobileData[uid]
                } else packageStats.mobileData = DataUsage.EMPTY
                if (wifiData.containsKey(uid)) {
                    packageStats.wifiData = wifiData[uid]
                } else packageStats.wifiData = DataUsage.EMPTY

                list.add(packageStats)

                progress.postValue(list.size)
            }.getOrElse {
                it.printStackTrace()
            }
        }

        return list
    }

    private fun getUsageEvents(): ArrayList<PackageStats> {
        val screenTimeList: ArrayList<PackageStats> = ArrayList()
        val screenTimes = HashMap<String, Long>()
        val lastUse = HashMap<String, Long>()
        val accessCount = HashMap<String, Int>()

        val interval = UsageInterval.getTimeInterval()
        val events: UsageEvents = usageStatsManager.queryEvents(interval.startTime, interval.endTime)
        val event = UsageEvents.Event()

        var startTime: Long
        var endTime: Long
        var skipNew = false

        while (events.hasNextEvent()) {
            if (!skipNew) events.getNextEvent(event)

            var eventTime = event.timeStamp
            val packageName = event.packageName

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

                            if (screenTimes.containsKey(packageName)) {
                                screenTimes[packageName] = screenTimes[packageName]!! + time
                            } else {
                                screenTimes[packageName] = time
                            }

                            lastUse[packageName] = endTime

                            if (accessCount.containsKey(packageName)) {
                                accessCount[packageName] = accessCount[packageName]!! + 1
                            } else {
                                accessCount[packageName] = 1
                            }
                        }
                        break
                    }
                }
            }
        }

        var mobileData = SparseArrayCompat<DataUsage>()
        var wifiData = SparseArrayCompat<DataUsage>()

        kotlin.runCatching {
            mobileData = getMobileData(StatisticsPreferences.getInterval())
        }
        kotlin.runCatching {
            wifiData = getWifiData(StatisticsPreferences.getInterval())
        }

        for (packageName in screenTimes.keys) {
            // Skip uninstalled packages?
            if (!packageManager.isPackageInstalled(packageName)) {
                continue
            }

            val packageStats = PackageStats()
            packageStats.packageInfo = packageManager.getPackageInfo(packageName)

            packageStats.launchCount = accessCount[packageName] ?: 0
            packageStats.lastUsageTime = lastUse[packageName] ?: 0
            packageStats.totalTimeUsed = screenTimes[packageName] ?: 0
            val uid: Int = packageStats.packageInfo?.applicationInfo?.uid!!

            if (mobileData.containsKey(uid)) {
                packageStats.mobileData = mobileData[uid]
            } else packageStats.mobileData = DataUsage.EMPTY
            if (wifiData.containsKey(uid)) {
                packageStats.wifiData = wifiData[uid]
            } else packageStats.wifiData = DataUsage.EMPTY

            screenTimeList.add(packageStats)
        }
        return screenTimeList
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        loadAppStats()
    }
}
