package app.simple.inure.dialogs.usagestats

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.usagestats.PopupAppsCategoryUsageStats
import app.simple.inure.popups.usagestats.PopupUsageIntervals
import app.simple.inure.popups.usagestats.PopupUsageStatsSorting
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.SortUsageStats
import app.simple.inure.util.UsageInterval

class UsageStatsMenu : ScopedBottomSheetFragment() {

    private lateinit var sort: DynamicRippleTextView
    private lateinit var category: DynamicRippleTextView
    private lateinit var settings: DynamicRippleTextView
    private lateinit var interval: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_usage_settings, container, false)

        sort = view.findViewById(R.id.dialog_apps_sorting)
        category = view.findViewById(R.id.dialog_apps_category)
        settings = view.findViewById(R.id.dialog_open_apps_settings)
        interval = view.findViewById(R.id.popup_interval)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSortText()
        setCategoryText()
        setIntervalText()

        sort.setOnClickListener {
            PopupUsageStatsSorting(it)
        }

        category.setOnClickListener {
            PopupAppsCategoryUsageStats(it)
        }

        interval.setOnClickListener {
            PopupUsageIntervals(it)
        }

        settings.setOnClickListener {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("main_preferences_screen")
                ?: MainPreferencesScreen.newInstance()

            requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.app_container, fragment, "main_preferences_screen")
                    .addToBackStack(tag)
                    .commit()
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

    private fun setCategoryText() {
        category.text = when (StatsPreferences.getAppsCategory()) {
            PopupAppsCategoryUsageStats.USER -> getString(R.string.user)
            PopupAppsCategoryUsageStats.SYSTEM -> getString(R.string.system)
            PopupAppsCategoryUsageStats.BOTH -> getString(R.string.both)
            else -> getString(R.string.unknown)
        }
    }

    private fun setIntervalText() {
        interval.text = when (StatsPreferences.getInterval()) {
            UsageInterval.TODAY -> getString(R.string.today)
            UsageInterval.DAILY -> getString(R.string.daily)
            UsageInterval.WEEKlY -> getString(R.string.weekly)
            UsageInterval.MONTHLY -> getString(R.string.monthly)
            UsageInterval.YEARLY -> getString(R.string.yearly)
            else -> getString(R.string.weekly)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatsPreferences.statsSorting -> {
                setSortText()
            }
            StatsPreferences.appsCategory -> {
                setCategoryText()
            }
            StatsPreferences.statsInterval -> {
                setIntervalText()
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
