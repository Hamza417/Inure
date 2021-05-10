package app.simple.inure.popups.app

import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.TypeFaceTextView

class PopupAnalytics(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {
        init(contentView, view)

        contentView.findViewById<TypeFaceTextView>(R.id.popup_analytics_refresh)
                .onClick(contentView.context.getString(R.string.refresh))
    }

    fun TypeFaceTextView.onClick(string: String) {
        setOnClickListener {
            dismiss()
            popupMenuCallback.onMenuItemClicked(string)
        }
    }

    fun setOnPopupMenuCallback(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}