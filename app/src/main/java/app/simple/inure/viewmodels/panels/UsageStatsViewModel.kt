package app.simple.inure.viewmodels.panels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.usage.*
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import android.os.RemoteException
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.IntDef
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.DataUsage
import app.simple.inure.models.PackageStats
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.popups.usagestats.PopupUsageStatsEngine
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.PermissionUtils
import app.simple.inure.util.SortUsageStats.sortStats
import app.simple.inure.util.UsageInterval
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors

class UsageStatsViewModel(application: Application) : WrappedViewModel(application) {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(value = [TRANSPORT_CELLULAR, TRANSPORT_WIFI])
    annotation class Transport

    private var usageStatsManager: UsageStatsManager = getApplication<Application>()
        .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var networkStatsManager = getApplication<Application>()
        .getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

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
                app.packageInfo!!.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageInfo!!.applicationInfo)
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

        var apps = getApplication<Application>().packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

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
        val accessCount = HashMap<String, Long>()

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
            val packageStats = PackageStats()
            packageStats.packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)

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

    private fun getMobileData(@UsageInterval.IntervalType intervalType: Int): SparseArrayCompat<DataUsage> {
        return getDataUsageForNetwork(TRANSPORT_CELLULAR, intervalType)
    }

    private fun getWifiData(@UsageInterval.IntervalType intervalType: Int): SparseArrayCompat<DataUsage> {
        return getDataUsageForNetwork(TRANSPORT_WIFI, intervalType)
    }

    private fun getDataUsageForNetwork(@Transport networkType: Int, @UsageInterval.IntervalType intervalType: Int): SparseArrayCompat<DataUsage> {
        val dataUsageSparseArray = SparseArrayCompat<DataUsage>()
        val range: UsageInterval.UsageInterval = UsageInterval.getTimeInterval(intervalType)
        // val subscriberIds: List<String?> = getSubscriberIds(context, networkType)
        try {
            val bucket = NetworkStats.Bucket()
            networkStatsManager.querySummary(networkType, null, range.startTime, range.endTime).use { networkStats ->
                if (networkStats != null) {
                    while (networkStats.hasNextBucket()) {
                        networkStats.getNextBucket(bucket)
                        var dataUsage = dataUsageSparseArray[bucket.uid]
                        dataUsage = if (dataUsage != null) {
                            DataUsage(bucket.txBytes + dataUsage.tx, bucket.rxBytes + dataUsage.rx)
                        } else {
                            DataUsage(bucket.txBytes, bucket.rxBytes)
                        }
                        dataUsageSparseArray.put(bucket.uid, dataUsage)
                    }
                }
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return dataUsageSparseArray
    }

    /**
     * @return A list of subscriber IDs if networkType is [android.net.NetworkCapabilities.TRANSPORT_CELLULAR], or
     * a singleton array with `null` being the only element.
     */
    @SuppressLint("HardwareIds", "MissingPermission")
    @Deprecated("Requires {@code android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE} from Android 10 (API 29)")
    private fun getSubscriberIds(context: Context, @Transport networkType: Int): List<String?> {
        return if (networkType != TRANSPORT_CELLULAR || !PermissionUtils.hasPermission(applicationContext(), Manifest.permission.READ_PHONE_STATE)) {
            Collections.singletonList(null)
        } else try {
            val subscriptionManager = applicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val telephonyManager = Objects.requireNonNull(applicationContext().getSystemService(Context.TELEPHONY_SERVICE)) as TelephonyManager
            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList ?: /* No telephony services */ return Collections.singletonList(null)
            val subscriberIds: MutableList<String> = ArrayList()
            for (info in subscriptionInfoList) {
                val subscriptionId = info.subscriptionId
                try {
                    val subscriberId: String = Refine.unsafeCast<TelephonyManager>(telephonyManager).subscriberId
                    subscriberIds.add(subscriberId)
                } catch (e: Exception) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            subscriberIds.add(telephonyManager.createForSubscriptionId(subscriptionId).subscriberId)
                        }
                    } catch (e2: Exception) {
                        subscriberIds.add(telephonyManager.subscriberId)
                    }
                }
            }
            if (subscriberIds.size == 0) Collections.singletonList(null) else subscriberIds
        } catch (e: SecurityException) {
            Collections.singletonList(null)
        }
        // FIXME: 24/4/21 Consider using Binder to fetch subscriber info
    }
}
