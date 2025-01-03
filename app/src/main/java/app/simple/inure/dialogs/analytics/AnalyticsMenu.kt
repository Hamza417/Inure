package app.simple.inure.dialogs.analytics

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.analytics.PopupSdkValue
import app.simple.inure.preferences.AnalyticsPreferences

class AnalyticsMenu : ScopedBottomSheetFragment() {

    private lateinit var sdkValue: DynamicRippleTextView
    private lateinit var pieHoleRadius: DynamicRippleTextView
    private lateinit var chartLabelsSwitch: Switch
    private lateinit var settings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_analytics, container, false)

        sdkValue = view.findViewById(R.id.sdk_value)
        pieHoleRadius = view.findViewById(R.id.dialog_open_pie_hole)
        chartLabelsSwitch = view.findViewById(R.id.chart_labels_switch)
        settings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSdkValue()
        chartLabelsSwitch.isChecked = AnalyticsPreferences.getChartLabel()

        sdkValue.setOnClickListener {
            PopupSdkValue(it)
        }

        pieHoleRadius.setOnClickListener {
            PieHoleRadius.newInstance()
                .show(parentFragmentManager, "pie_hole_radius").also {
                    dismiss()
                }
        }

        chartLabelsSwitch.setOnSwitchCheckedChangeListener {
            AnalyticsPreferences.setChartLabel(it)
        }

        settings.setOnClickListener {
            openSettings()
        }
    }

    private fun setSdkValue() {
        sdkValue.text = if (AnalyticsPreferences.getSDKValue()) {
            getString(R.string.version_code)
        } else {
            getString(R.string.version_name)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AnalyticsPreferences.SDK_VALUE -> {
                setSdkValue()
            }
        }
    }

    companion object {
        fun newInstance(): AnalyticsMenu {
            val args = Bundle()
            val fragment = AnalyticsMenu()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "analytics_menu"

        fun FragmentManager.showAnalyticsMenu() {
            newInstance().show(this, TAG)
        }
    }
}
