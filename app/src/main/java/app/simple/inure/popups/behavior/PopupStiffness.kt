package app.simple.inure.popups.behavior

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.BehaviourPreferences

class PopupStiffness(view: View) : BasePopupWindow() {

    private val veryLow: DynamicRippleTextView
    private val low: DynamicRippleTextView
    private val medium: DynamicRippleTextView
    private val high: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_stiffness, PopupLinearLayout(view.context))

        veryLow = contentView.findViewById(R.id.popup_very_low)
        low = contentView.findViewById(R.id.popup_low)
        medium = contentView.findViewById(R.id.popup_medium)
        high = contentView.findViewById(R.id.popup_high)

        veryLow.onClick(SpringForce.STIFFNESS_VERY_LOW)
        low.onClick(SpringForce.STIFFNESS_LOW)
        medium.onClick(SpringForce.STIFFNESS_MEDIUM)
        high.onClick(SpringForce.STIFFNESS_HIGH)

        when (BehaviourPreferences.getStiffness()) {
            SpringForce.STIFFNESS_VERY_LOW -> veryLow.isSelected = true
            SpringForce.STIFFNESS_LOW -> low.isSelected = true
            SpringForce.STIFFNESS_MEDIUM -> medium.isSelected = true
            SpringForce.STIFFNESS_HIGH -> high.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(category: Float) {
        this.setOnClickListener {
            BehaviourPreferences.setStiffness(category)
            dismiss()
        }
    }
}