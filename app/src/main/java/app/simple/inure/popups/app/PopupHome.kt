package app.simple.inure.popups.app

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.extension.popup.PopupMenuCallback

class PopupHome(view: View) : BasePopupWindow() {
    private lateinit var popupMenuCallback: PopupMenuCallback

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_home_menu, PopupLinearLayout(view.context))

        contentView.findViewById<TypeFaceTextView>(R.id.popup_home_refresh)
            .onClick(contentView.context.getString(R.string.refresh))

        contentView.findViewById<TypeFaceTextView>(R.id.popup_home_prefs)
            .onClick(contentView.context.getString(R.string.preferences))

        init(contentView, view)
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