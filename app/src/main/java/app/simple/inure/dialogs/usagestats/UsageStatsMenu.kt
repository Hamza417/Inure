package app.simple.inure.dialogs.usagestats

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.usagestats.UsageStatsSort.Companion.showUsageStatsSort
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.usagestats.PopupUsageIntervals
import app.simple.inure.popups.usagestats.PopupUsageStatsEngine
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.UsageInterval

class UsageStatsMenu : ScopedBottomSheetFragment() {

    private lateinit var engine: DynamicRippleTextView
    private lateinit var settings: DynamicRippleTextView
    private lateinit var interval: DynamicRippleTextView
    private lateinit var unusedAppsToggle: Switch
    private lateinit var limitToHours: Switch
    private lateinit var filter: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_usage_stats, container, false)

        engine = view.findViewById(R.id.popup_usage_engine)
        settings = view.findViewById(R.id.dialog_open_apps_settings)
        interval = view.findViewById(R.id.popup_interval)
        unusedAppsToggle = view.findViewById(R.id.hide_unused_switch)
        limitToHours = view.findViewById(R.id.limit_hours_switch)
        filter = view.findViewById(R.id.filter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setIntervalText()
        setEngineText()
        unusedAppsToggle.isChecked = StatisticsPreferences.areUnusedAppHidden()
        limitToHours.isChecked = StatisticsPreferences.isLimitToHours()

        engine.setOnClickListener {
            PopupUsageStatsEngine(it)
        }

        interval.setOnClickListener {
            PopupUsageIntervals(it)
        }

        unusedAppsToggle.setOnSwitchCheckedChangeListener {
            StatisticsPreferences.setUnusedAppState(it)
        }

        limitToHours.setOnSwitchCheckedChangeListener {
            StatisticsPreferences.setLimitToHours(it)
        }

        filter.setOnClickListener {
            parentFragmentManager.showUsageStatsSort()
            dismiss()
        }

        settings.setOnClickListener {
            openSettings()
        }
    }

    private fun setIntervalText() {
        interval.text = when (StatisticsPreferences.getInterval()) {
            UsageInterval.DAILY -> getString(R.string.daily)
            UsageInterval.WEEKlY -> getString(R.string.weekly)
            UsageInterval.MONTHLY -> getString(R.string.monthly)
            UsageInterval.YEARLY -> getString(R.string.yearly)
            else -> getString(R.string.unknown)
        }
    }

    private fun setEngineText() {
        engine.text = when (StatisticsPreferences.getEngine()) {
            PopupUsageStatsEngine.INURE -> getString(R.string.app_name)
            PopupUsageStatsEngine.ANDROID -> getString(R.string.android)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatisticsPreferences.STATS_INTERVAL -> {
                setIntervalText()
            }
            StatisticsPreferences.STATS_ENGINE -> {
                setEngineText()
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
