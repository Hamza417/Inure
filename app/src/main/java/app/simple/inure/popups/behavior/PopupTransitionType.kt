package app.simple.inure.popups.behavior

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.BehaviourPreferences

class PopupTransitionType(view: View) : BasePopupWindow() {

    private val fade: DynamicRippleTextView
    private val elevation: DynamicRippleTextView
    private val sharedAxis: DynamicRippleTextView
    private val through: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_transitions, PopupLinearLayout(view.context))

        fade = contentView.findViewById(R.id.popup_fade)
        elevation = contentView.findViewById(R.id.popup_elevation)
        sharedAxis = contentView.findViewById(R.id.popup_shared_axis)
        through = contentView.findViewById(R.id.popup_through)

        fade.onClick(FADE)
        elevation.onClick(ELEVATION)
        sharedAxis.onClick(SHARED_AXIS)
        through.onClick(THROUGH)

        when (BehaviourPreferences.getTransitionType()) {
            FADE -> fade.isSelected = true
            ELEVATION -> elevation.isSelected = true
            SHARED_AXIS -> sharedAxis.isSelected = true
            THROUGH -> through.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(category: Int) {
        this.setOnClickListener {
            BehaviourPreferences.setTransitionType(category)
            dismiss()
        }
    }

    companion object {
        const val FADE = 0
        const val ELEVATION = 1
        const val SHARED_AXIS = 2
        const val THROUGH = 3
    }
}