package app.simple.inure.popups.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout

@Deprecated("Use bottom sheet modal dialog for better UX.")
class PopupSure(view: View) : BasePopupWindow() {

    var onSure: (() -> Unit)? = null

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_sure, PopupLinearLayout(view.context))

        contentView.findViewById<DynamicRippleTextView>(R.id.sure_affirmative).onClick()

        contentView.findViewById<DynamicRippleTextView>(R.id.sure_negative).setOnClickListener {
            dismiss()
        }

        init(contentView, view, Gravity.TOP and Gravity.END)
    }

    private fun DynamicRippleTextView.onClick() {
        this.setOnClickListener {
            onSure?.invoke()
            dismiss()
        }
    }
}