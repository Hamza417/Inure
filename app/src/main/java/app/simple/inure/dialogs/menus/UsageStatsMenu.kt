package app.simple.inure.dialogs.menus

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.usagestats.PopupUsageIntervals
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.UsageInterval

class UsageStatsMenu : ScopedBottomSheetFragment() {

    private lateinit var settings: DynamicRippleTextView
    private lateinit var interval: DynamicRippleTextView
    private lateinit var unusedAppsToggle: SwitchView
    private lateinit var limitToHours: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_usage_settings, container, false)

        settings = view.findViewById(R.id.dialog_open_apps_settings)
        interval = view.findViewById(R.id.popup_interval)
        unusedAppsToggle = view.findViewById(R.id.hide_unused_switch)
        limitToHours = view.findViewById(R.id.limit_hours_switch)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setIntervalText()
        unusedAppsToggle.setChecked(StatisticsPreferences.areUnusedAppHidden())
        limitToHours.setChecked(StatisticsPreferences.isLimitToHours())

        interval.setOnClickListener {
            PopupUsageIntervals(it)
        }

        unusedAppsToggle.setOnSwitchCheckedChangeListener {
            StatisticsPreferences.setUnusedAppState(it)
        }

        limitToHours.setOnSwitchCheckedChangeListener {
            StatisticsPreferences.setLimitToHours(it)
        }

        settings.setOnClickListener {
            (parentFragment as ScopedFragment).clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Preferences.newInstance(),
                                        "prefs")
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatisticsPreferences.statsInterval -> {
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