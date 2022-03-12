package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.UsageInterval
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AppStatisticsViewModel(application: Application, private val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private var usageStatsManager: UsageStatsManager = getApplication<Application>()
        .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var networkStatsManager = getApplication<Application>()
        .getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    private val totalUsedChartData: MutableLiveData<List<BarEntry>> by lazy {
        MutableLiveData<List<BarEntry>>().also {
            loadStatsData()
        }
    }

    private val totalAppSize: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>().also {
            loadTotalAppSize()
        }
    }

    fun getTotalUsedChartData(): LiveData<List<BarEntry>> {
        return totalUsedChartData
    }

    private fun loadStatsData() {
        viewModelScope.launch(Dispatchers.Default) {
            val stats: MutableList<UsageStats> = with(UsageInterval.getTimeInterval()) {
                usageStatsManager.queryUsageStats(StatisticsPreferences.getInterval(), first, second)
            }

            val p0 = stats.stream().filter {
                it.packageName == packageInfo.packageName
            }.collect(Collectors.toList())

            stats[0].totalTimeInForeground

            val list = arrayListOf<BarEntry>()

            for (data in p0.indices) {
                list.add(BarEntry(data.toFloat(), p0[data].totalTimeInForeground.toFloat()))
            }

            totalUsedChartData.postValue(list)
        }
    }

    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = getApplication<Application>()
                .applicationContext.packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList

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
