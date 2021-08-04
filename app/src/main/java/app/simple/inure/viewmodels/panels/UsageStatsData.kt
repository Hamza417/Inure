package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.model.PackageStats
import app.simple.inure.util.SortUsageStats.sortStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

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
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            val start: Long = calendar.timeInMillis
            val end = System.currentTimeMillis()

            val list = arrayListOf<PackageStats>()
            val stats = usageStatsManager.queryAndAggregateUsageStats(start, end)
            val apps = getApplication<Application>()
                    .packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

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

            list.sortStats()

            usageData.postValue(list)
        }
    }

    private fun getInternetUsage(packageInfo: PackageInfo, packageStats: PackageStats) {
        val bucketWifi = networkStatsManager
                .queryDetailsForUid(NetworkCapabilities.TRANSPORT_WIFI,
                                    "",
                                    packageInfo.firstInstallTime,
                                    System.currentTimeMillis(),
                                    packageInfo.applicationInfo.uid)

        val bucketMobile = networkStatsManager
                .queryDetailsForUid(NetworkCapabilities.TRANSPORT_CELLULAR,
                                    null,
                                    packageInfo.firstInstallTime,
                                    System.currentTimeMillis(),
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
