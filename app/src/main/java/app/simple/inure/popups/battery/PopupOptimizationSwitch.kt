package app.simple.inure.popups.battery

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.models.BatteryOptimizationModel

class PopupOptimizationSwitch(view: View, batteryOptimizationModel: BatteryOptimizationModel) : BasePopupWindow() {

    private val optimize: DynamicRippleTextView

    init {
        val contentView = View.inflate(view.context, R.layout.popup_optimization_switch, PopupLinearLayout(view.context))

        optimize = contentView.findViewById(R.id.popup_optimize_state)

        if (batteryOptimizationModel.isOptimized) {
            optimize.text = contentView.context.getString(R.string.dont_optimize)
        } else {
            optimize.text = contentView.context.getString(R.string.optimize)
        }

        contentView.requestLayout()
        contentView.invalidate()

        init(contentView, view, Gravity.END)
    }

    fun setOnOptimizeClicked(callback: () -> Unit) {
        optimize.setOnClickListener {
            callback.invoke()
            dismiss()
        }
    }
}