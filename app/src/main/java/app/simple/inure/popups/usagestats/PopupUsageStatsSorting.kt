package app.simple.inure.popups.usagestats

import android.view.View
import android.widget.CheckBox
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.util.SortUsageStats

class PopupUsageStatsSorting(contentView: View, view: View) : BasePopupWindow() {

    init {
        init(contentView, view)

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_name).setOnClickListener {
            setOnClick(SortUsageStats.NAME)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_time_used).setOnClickListener {
            setOnClick(SortUsageStats.TIME)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_data_sent).setOnClickListener {
            setOnClick(SortUsageStats.DATA_SENT)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_data_received).setOnClickListener {
            setOnClick(SortUsageStats.DATA_RECEIVED)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_wifi_sent).setOnClickListener {
            setOnClick(SortUsageStats.WIFI_SENT)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_wifi_received).setOnClickListener {
            setOnClick(SortUsageStats.WIFI_RECEIVED)
        }

        with(contentView.findViewById<CheckBox>(R.id.sort_reversed_checkbox)) {
            isChecked = StatsPreferences.isReverseSorting()

            setOnCheckedChangeListener { _, isChecked ->
                StatsPreferences.setReverseSorting(isChecked)
            }
        }
    }

    private fun setOnClick(source: String) {
        StatsPreferences.setSortType(source)
        dismiss()
    }
}