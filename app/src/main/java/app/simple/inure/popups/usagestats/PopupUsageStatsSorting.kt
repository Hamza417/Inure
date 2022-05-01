package app.simple.inure.popups.usagestats

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.CheckBox
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.SortUsageStats

class PopupUsageStatsSorting(view: View) : BasePopupWindow() {

    private val name: DynamicRippleTextView
    private val time: DynamicRippleTextView
    private val dataSent: DynamicRippleTextView
    private val dataReceived: DynamicRippleTextView
    private val wifiSent: DynamicRippleTextView
    private val wifiReceived: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_usage_stats_sorting, PopupLinearLayout(view.context))

        name = contentView.findViewById(R.id.sort_name)
        time = contentView.findViewById(R.id.sort_time_used)
        dataSent = contentView.findViewById(R.id.sort_data_sent)
        dataReceived = contentView.findViewById(R.id.sort_data_received)
        wifiSent = contentView.findViewById(R.id.sort_wifi_sent)
        wifiReceived = contentView.findViewById(R.id.sort_wifi_received)

        name.setOnClickListener {
            setOnClick(SortUsageStats.NAME)
        }

        time.setOnClickListener {
            setOnClick(SortUsageStats.TIME)
        }

        dataSent.setOnClickListener {
            setOnClick(SortUsageStats.DATA_SENT)
        }

        dataReceived.setOnClickListener {
            setOnClick(SortUsageStats.DATA_RECEIVED)
        }

        wifiSent.setOnClickListener {
            setOnClick(SortUsageStats.WIFI_SENT)
        }

        wifiReceived.setOnClickListener {
            setOnClick(SortUsageStats.WIFI_RECEIVED)
        }

        when (StatisticsPreferences.getSortedBy()) {
            SortUsageStats.NAME -> name.isSelected = true
            SortUsageStats.TIME -> time.isSelected = true
            SortUsageStats.DATA_SENT -> dataSent.isSelected = true
            SortUsageStats.DATA_RECEIVED -> dataReceived.isSelected = true
            SortUsageStats.WIFI_SENT -> wifiSent.isSelected = true
            SortUsageStats.WIFI_RECEIVED -> wifiReceived.isSelected = true
        }

        with(contentView.findViewById<CheckBox>(R.id.sort_reversed_checkbox)) {
            setChecked(StatisticsPreferences.isReverseSorting())

            setOnCheckedChangeListener { isChecked ->
                StatisticsPreferences.setReverseSorting(isChecked)
            }
        }

        init(contentView, view)
    }

    private fun setOnClick(source: String) {
        StatisticsPreferences.setSortType(source)
        dismiss()
    }
}