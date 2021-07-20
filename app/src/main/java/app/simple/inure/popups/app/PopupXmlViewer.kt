package app.simple.inure.popups.app

import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class PopupXmlViewer(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var popupXmlCallbacks: PopupXmlCallbacks

    init {
        init(contentView, view)

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_xml_copy).onClick(contentView.context.getString(R.string.copy))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_xml_save).onClick(contentView.context.getString(R.string.save))
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