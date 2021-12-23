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
import app.simple.inure.popups.usagestats.PopupUsageIntervals
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.UsageInterval

class UsageStatsMenu : ScopedBottomSheetFragment() {

    private lateinit var settings: DynamicRippleTextView
    private lateinit var interval: DynamicRippleTextView
    private lateinit var unusedAppsToggle: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_usage_settings, container, false)

        settings = view.findViewById(R.id.dialog_open_apps_settings)
        interval = view.findViewById(R.id.popup_interval)
        unusedAppsToggle = view.findViewById(R.id.hide_unused_switch)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setIntervalText()
        unusedAppsToggle.setChecked(StatisticsPreferences.areUnusedAppHidden())

        interval.setOnClickListener {
            PopupUsageIntervals(it)
        }

        unusedAppsToggle.setOnSwitchCheckedChangeListener {
            StatisticsPreferences.setUnusedAppState(it)
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