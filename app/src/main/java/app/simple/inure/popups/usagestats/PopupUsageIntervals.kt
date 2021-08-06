package app.simple.inure.popups.usagestats

import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.util.UsageInterval

class PopupUsageIntervals(contentView: View, view: View) : BasePopupWindow() {
    init {
        init(contentView, view)

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_interval_today).setOnClickListener {
            setInterval(UsageInterval.TODAY)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_interval_daily).setOnClickListener {
            setInterval(UsageInterval.DAILY)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_interval_weekly).setOnClickListener {
            setInterval(UsageInterval.WEEKlY)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_interval_monthly).setOnClickListener {
            setInterval(UsageInterval.MONTHLY)
        }
    }

    private fun setInterval(interval: String) {
        StatsPreferences.setInterval(interval).also {
            dismiss()
        }
    }
}