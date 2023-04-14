package app.simple.inure.popups.viewers

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupImageViewer(view: View) : BasePopupWindow() {

    private lateinit var popupXmlCallbacks: PopupImageCallbacks

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_image_viewer, PopupLinearLayout(view.context))

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_xml_export).onClick(contentView.context.getString(R.string.export))

        init(contentView, view, Gravity.START)
    }

    private fun DynamicRippleTextView.onClick(s: String) {
        this.setOnClickListener {
            popupXmlCallbacks.onPopupItemClicked(s)
            dismiss()
        }
    }

    fun setOnPopupClickedListener(popupXmlCallbacks: PopupImageCallbacks) {
        this.popupXmlCallbacks = popupXmlCallbacks
    }

    interface PopupImageCallbacks {
        fun onPopupItemClicked(source: String)
    }
}