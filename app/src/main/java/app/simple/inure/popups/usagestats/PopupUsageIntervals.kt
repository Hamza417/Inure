package app.simple.inure.popups.usagestats

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.UsageInterval

class PopupUsageIntervals(view: View) : BasePopupWindow() {

    private val daily: DynamicRippleTextView
    private val weekly: DynamicRippleTextView
    private val monthly: DynamicRippleTextView
    private val yearly: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_usage_stats_interval, PopupLinearLayout(view.context))
        init(contentView, view)

        daily = contentView.findViewById(R.id.popup_interval_daily)
        weekly = contentView.findViewById(R.id.popup_interval_weekly)
        monthly = contentView.findViewById(R.id.popup_interval_monthly)
        yearly = contentView.findViewById(R.id.popup_interval_yearly)

        daily.setOnClickListener {
            setInterval(UsageInterval.DAILY)
        }

        weekly.setOnClickListener {
            setInterval(UsageInterval.WEEKlY)
        }

        monthly.setOnClickListener {
            setInterval(UsageInterval.MONTHLY)
        }

        yearly.setOnClickListener {
            setInterval(UsageInterval.YEARLY)
        }

        when (StatisticsPreferences.getInterval()) {
            UsageInterval.DAILY -> daily.isSelected = true
            UsageInterval.WEEKlY -> weekly.isSelected = true
            UsageInterval.MONTHLY -> monthly.isSelected = true
            UsageInterval.YEARLY -> yearly.isSelected = true
        }
    }

    private fun setInterval(interval: Int) {
        StatisticsPreferences.setInterval(interval).also {
            dismiss()
        }
    }
}