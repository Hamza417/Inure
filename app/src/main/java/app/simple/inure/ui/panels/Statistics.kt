package app.simple.inure.ui.panels

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.StatsPreferences
import com.topjohnwu.superuser.internal.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class Statistics : ScopedFragment() {

    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        usageStatsManager = requireActivity().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {
            withContext(Dispatchers.Default) {
                val calendar: Calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1)
                val start: Long = calendar.timeInMillis
                val end = System.currentTimeMillis()
                val stats: List<UsageStats> = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, start, end)

                for (i in stats) {
                    println(i.packageName)
                    println(i.totalTimeVisible)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            StatsPreferences.statsInterval -> {

            }
        }
    }

    companion object {
        fun newInstance(): Statistics {
            val args = Bundle()
            val fragment = Statistics()
            fragment.arguments = args
            return fragment
        }
    }
}