package app.simple.inure.popups.behavior

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.BehaviourPreferences

class PopupArcType(view: View) : BasePopupWindow() {

    private val inure: DynamicRippleTextView
    private val material: DynamicRippleTextView
    private val legacy: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_arc_type, PopupLinearLayout(view.context))

        inure = contentView.findViewById(R.id.popup_inure)
        material = contentView.findViewById(R.id.popup_material)
        legacy = contentView.findViewById(R.id.popup_legacy)

        inure.onClick(INURE)
        material.onClick(MATERIAL)
        legacy.onClick(LEGACY)

        when (BehaviourPreferences.getArcType()) {
            INURE -> inure.isSelected = true
            MATERIAL -> material.isSelected = true
            LEGACY -> legacy.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(category: Int) {
        this.setOnClickListener {
            BehaviourPreferences.setArcType(category)
            dismiss()
        }
    }

    companion object {
        const val INURE = 0
        const val MATERIAL = 1
        const val LEGACY = 2
    }
}