package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.model.PackageStats
import app.simple.inure.util.FileSizeHelper.toSize
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

            max.postValue(stats.size)

            for (i in stats) {
                kotlin.runCatching {
                    val packageStats = PackageStats()

                    packageStats.packageInfo = getApplication<Application>()
                            .packageManager.getPackageInfo(i.value.packageName, PackageManager.GET_META_DATA)

                    packageStats.packageInfo!!.applicationInfo.apply {
                        name = getApplication<Application>().packageManager.getApplicationLabel(this).toString()
                    }

                    packageStats.totalTimeUsed = i.value.totalTimeInForeground

                    getInternetUsage(packageStats.packageInfo!!, packageStats)

                    list.add(packageStats)

                    progress.postValue(list.size)
                }.getOrElse {
                    it.printStackTrace()
                }
            }

            list.sortBy {
                it.packageInfo!!.applicationInfo.name
            }

            usageData.postValue(list)
        }
    }

    private fun getInternetUsage(packageInfo: PackageInfo, packageStats: PackageStats) {
        val bucketWifi = networkStatsManager
                .queryDetailsForUid(ConnectivityManager.TYPE_WIFI,
                                    "",
                                    packageInfo.firstInstallTime,
                                    System.currentTimeMillis(),
                                    packageInfo.applicationInfo.uid)

        val bucketMobile = networkStatsManager
                .queryDetailsForUid(ConnectivityManager.TYPE_MOBILE,
                                    null,
                                    packageInfo.firstInstallTime,
                                    System.currentTimeMillis(),
                                    packageInfo.applicationInfo.uid)

        val bucket = NetworkStats.Bucket()
        bucketMobile.getNextBucket(bucket)

        packageStats.dataReceived = bucket.rxBytes
        packageStats.dataSent = bucket.txBytes

        bucketWifi.getNextBucket(bucket)

        packageStats.dataReceivedWifi = bucket.rxBytes
        packageStats.dataSentWifi = bucket.txBytes

        bucketWifi.close()
        bucketMobile.close()
    }
}
