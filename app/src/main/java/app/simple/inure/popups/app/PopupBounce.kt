package app.simple.inure.popups.app

import android.view.Gravity
import android.view.View
import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.preferences.AppearancePreferences

class PopupBounce(contentView: View, view: View) : BasePopupWindow() {

    init {
        init(contentView, view, Gravity.START)

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_bounce_no_bounce).onClick(SpringForce.DAMPING_RATIO_NO_BOUNCY)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_bounce_low).onClick(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_bounce_medium).onClick(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_bounce_high).onClick(0.4F)
    }

    private fun DynamicRippleTextView.onClick(bouncyValue: Float) {
        this.setOnClickListener {
            dismiss()
        }
    }
}