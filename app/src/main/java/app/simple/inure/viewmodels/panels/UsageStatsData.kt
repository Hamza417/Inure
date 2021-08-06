package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.model.PackageStats
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.util.SortUsageStats.sortStats
import app.simple.inure.util.UsageInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors

class UsageStatsData(application: Application) : AndroidViewModel(application) {

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
            var list = arrayListOf<PackageStats>()
            val stats = with(UsageInterval.getTimeInterval()) {
                usageStatsManager.queryAndAggregateUsageStats(first, second)
            }

            var apps = getApplication<Application>()
                    .packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            when (StatsPreferences.getAppsCategory()) {
                AppCategoryPopup.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                AppCategoryPopup.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            max.postValue(stats.size)

            for (app in apps) {
                kotlin.runCatching {
                    val packageStats = PackageStats()

                    packageStats.packageInfo = app

                    packageStats.packageInfo!!.applicationInfo.apply {
                        name = getApplication<Application>().packageManager.getApplicationLabel(this).toString()
                    }

                    packageStats.totalTimeUsed += stats[app.packageName]?.totalTimeInForeground ?: 0

                    getInternetUsage(app, packageStats)

                    list.add(packageStats)

                    progress.postValue(list.size)
                }.getOrElse {
                    it.printStackTrace()
                }
            }

            list = list.filter {
                it.totalTimeUsed != 0L
            } as ArrayList<PackageStats>

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

    private fun getInternetUsage(packageInfo: PackageInfo, packageStats: PackageStats) {
        val interval = UsageInterval.getTimeInterval()

        val bucketWifi = networkStatsManager
                .queryDetailsForUid(NetworkCapabilities.TRANSPORT_WIFI,
                                    "",
                                    interval.first,
                                    interval.second,
                                    packageInfo.applicationInfo.uid)

        val bucketMobile = networkStatsManager
                .queryDetailsForUid(NetworkCapabilities.TRANSPORT_CELLULAR,
                                    null,
                                    interval.first,
                                    interval.second,
                                    packageInfo.applicationInfo.uid)


        with(NetworkStats.Bucket()) {
            while (bucketMobile.hasNextBucket()) {
                bucketMobile.getNextBucket(this)

                packageStats.dataReceived += rxBytes
                packageStats.dataSent += txBytes
            }
        }

        with(NetworkStats.Bucket()) {
            while (bucketWifi.hasNextBucket()) {
                bucketWifi.getNextBucket(this)

                packageStats.dataReceivedWifi += rxBytes
                packageStats.dataSentWifi += txBytes
            }
        }

        bucketWifi.close()
        bucketMobile.close()
    }
}
