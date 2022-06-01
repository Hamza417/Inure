package app.simple.inure.popups.viewers

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.PermissionPreferences

class PopupLabelType(view: View) : BasePopupWindow() {

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_label_type, PopupLinearLayout(view.context))

        val id = contentView.findViewById<DynamicRippleTextView>(R.id.popup_id)
        val descriptive = contentView.findViewById<DynamicRippleTextView>(R.id.popup_descriptive)

        id.setOnClickListener {
            PermissionPreferences.setLabelType(true)
            dismiss()
        }

        descriptive.setOnClickListener {
            PermissionPreferences.setLabelType(false)
            dismiss()
        }

        init(contentView, view, Gravity.END)
    }
}