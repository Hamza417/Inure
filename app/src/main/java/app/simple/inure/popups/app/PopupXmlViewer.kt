package app.simple.inure.popups.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupXmlViewer(view: View) : BasePopupWindow() {

    private lateinit var popupXmlCallbacks: PopupXmlCallbacks

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_xml_viewer_menu, PopupLinearLayout(view.context))

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_xml_copy).onClick(contentView.context.getString(R.string.copy))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_xml_save).onClick(contentView.context.getString(R.string.save))

        init(contentView, view, Gravity.NO_GRAVITY)
    }

    private fun DynamicRippleTextView.onClick(s: String) {
        this.setOnClickListener {
            popupXmlCallbacks.onPopupItemClicked(s)
            dismiss()
        }
    }

    fun setOnPopupClickedListener(popupXmlCallbacks: PopupXmlCallbacks) {
        this.popupXmlCallbacks = popupXmlCallbacks
    }

    interface PopupXmlCallbacks {
        fun onPopupItemClicked(source: String)
    }
}