package app.simple.inure.viewmodels.panels

import android.app.Application
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import app.simple.inure.model.PackageStats
import app.simple.inure.preferences.StatsPreferences
import java.util.*

class UsageStatsData(application: Application) : AndroidViewModel(application) {

    private var usageStatsManager: UsageStatsManager = getApplication<Application>().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private var networkStatsManager = getApplication<Application>().getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    val usageData: MutableLiveData<PackageStats> by lazy {
        MutableLiveData<PackageStats>().also {
            loadAppStats()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadAppStats() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val start: Long = calendar.timeInMillis
        val end = System.currentTimeMillis()
        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(StatsPreferences.getInterval(), start, end)

        val list = arrayListOf<PackageStats>()

        for (i in stats) {
            val packageStats = PackageStats()

            packageStats.applicationInfo = getApplication<Application>().packageManager.getApplicationInfo(i.packageName, PackageManager.GET_META_DATA)
            packageStats.totalTimeUsed = i.totalTimeVisible
        }
    }
}