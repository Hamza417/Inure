package app.simple.inure.ui.menus

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.app.PopupUsageStatsSorting
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.util.SortUsageStats

class UsageStatsMenu : ScopedBottomSheetFragment() {

    private lateinit var sort: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_usage_settings, container, false)

        sort = view.findViewById(R.id.dialog_apps_sorting)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSortText()

        sort.setOnClickListener {
            PopupUsageStatsSorting(layoutInflater.inflate(R.layout.popup_usage_stats_sorting,
                                                          DynamicCornerLinearLayout(requireContext())),
                                   it)
        }
    }

    private fun setSortText() {
        sort.text = when (StatsPreferences.getSortedBy()) {
            SortUsageStats.NAME -> getString(R.string.name)
            SortUsageStats.TIME -> getString(R.string.time_used)
            SortUsageStats.DATA_SENT -> getString(R.string.data_sent)
            SortUsageStats.DATA_RECEIVED -> getString(R.string.data_received)
            SortUsageStats.WIFI_SENT -> getString(R.string.wifi_sent)
            SortUsageStats.WIFI_RECEIVED -> getString(R.string.wifi_received)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatsPreferences.statsSorting -> {
                setSortText()
            }
        }
    }

    companion object {
        fun newInstance(): UsageStatsMenu {
            val args = Bundle()
            val fragment = UsageStatsMenu()
            fragment.arguments = args
            return fragment
        }
    }
}