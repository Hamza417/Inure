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
    private val sharedAxisX: DynamicRippleTextView
    private val sharedAxisY: DynamicRippleTextView
    private val sharedAxisZ: DynamicRippleTextView
    private val through: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_transitions, PopupLinearLayout(view.context))

        fade = contentView.findViewById(R.id.popup_fade)
        elevation = contentView.findViewById(R.id.popup_elevation)
        sharedAxisX = contentView.findViewById(R.id.popup_shared_axis_x)
        sharedAxisY = contentView.findViewById(R.id.popup_shared_axis_y)
        sharedAxisZ = contentView.findViewById(R.id.popup_shared_axis_z)
        through = contentView.findViewById(R.id.popup_through)

        sharedAxisX.text = contentView.context.getString(R.string.shared_axis, "X")
        sharedAxisY.text = contentView.context.getString(R.string.shared_axis, "Y")
        sharedAxisZ.text = contentView.context.getString(R.string.shared_axis, "Z")

        fade.onClick(FADE)
        elevation.onClick(ELEVATION)
        sharedAxisX.onClick(SHARED_AXIS_X)
        sharedAxisY.onClick(SHARED_AXIS_Y)
        sharedAxisZ.onClick(SHARED_AXIS_Z)
        through.onClick(THROUGH)

        when (BehaviourPreferences.getTransitionType()) {
            FADE -> fade.isSelected = true
            ELEVATION -> elevation.isSelected = true
            SHARED_AXIS_X -> sharedAxisX.isSelected = true
            SHARED_AXIS_Y -> sharedAxisY.isSelected = true
            SHARED_AXIS_Z -> sharedAxisZ.isSelected = true
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
        const val SHARED_AXIS_X = 2
        const val SHARED_AXIS_Y = 3
        const val SHARED_AXIS_Z = 4
        const val THROUGH = 5
    }
}