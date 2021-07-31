package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.StatisticsAdapter
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.viewmodels.panels.UsageStatsData

class Statistics : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var statisticsAdapter: StatisticsAdapter
    private val usageStatsData: UsageStatsData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        recyclerView = view.findViewById(R.id.usage_rv)

        statisticsAdapter = StatisticsAdapter()
        recyclerView.adapter = statisticsAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }

        usageStatsData.usageData.observe(viewLifecycleOwner, {
            statisticsAdapter.setData(it).also {
                recyclerView.setupFastScroller()
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
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
