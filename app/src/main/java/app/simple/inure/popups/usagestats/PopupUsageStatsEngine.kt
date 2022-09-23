package app.simple.inure.popups.usagestats

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.StatisticsPreferences

class PopupUsageStatsEngine(view: View) : BasePopupWindow() {

    private val inure: DynamicRippleTextView
    private val android: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_usage_stats_engine, PopupLinearLayout(view.context))
        init(contentView, view)

        inure = contentView.findViewById(R.id.popup_inure)
        android = contentView.findViewById(R.id.popup_android)

        inure.setOnClickListener {
            setInterval(INURE)
        }

        android.setOnClickListener {
            setInterval(ANDROID)
        }

        when (StatisticsPreferences.getEngine()) {
            INURE -> inure.isSelected = true
            ANDROID -> android.isSelected = true
        }
    }

    private fun setInterval(engine: String) {
        StatisticsPreferences.setEngine(engine).also {
            dismiss()
        }
    }

    companion object {
        const val INURE = "stats_inure"
        const val ANDROID = "stats_android"
    }
}