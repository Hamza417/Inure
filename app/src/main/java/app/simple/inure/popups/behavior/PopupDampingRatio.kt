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

class PopupDampingRatio(view: View) : BasePopupWindow() {

    private val none: DynamicRippleTextView
    private val low: DynamicRippleTextView
    private val medium: DynamicRippleTextView
    private val high: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_damping_ratio, PopupLinearLayout(view.context))

        none = contentView.findViewById(R.id.popup_none)
        low = contentView.findViewById(R.id.popup_low)
        medium = contentView.findViewById(R.id.popup_medium)
        high = contentView.findViewById(R.id.popup_high)

        none.onClick(SpringForce.DAMPING_RATIO_NO_BOUNCY)
        low.onClick(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
        medium.onClick(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
        high.onClick(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)

        when (BehaviourPreferences.getDampingRatio()) {
            SpringForce.DAMPING_RATIO_NO_BOUNCY -> none.isSelected = true
            SpringForce.DAMPING_RATIO_LOW_BOUNCY -> low.isSelected = true
            SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY -> medium.isSelected = true
            SpringForce.DAMPING_RATIO_HIGH_BOUNCY -> high.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(category: Float) {
        this.setOnClickListener {
            BehaviourPreferences.setDampingRatio(category)
            dismiss()
        }
    }
}