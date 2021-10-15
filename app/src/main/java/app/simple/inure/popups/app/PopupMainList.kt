package app.simple.inure.popups.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.typeface.TypeFaceTextView

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
class PopupMainList(anchor: View) : BasePopupWindow() {

    lateinit var popupMenuCallback: PopupMenuCallback

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_main_list_menu, PopupLinearLayout(anchor.context))

        init(contentView, anchor, Gravity.END)

        contentView.findViewById<TypeFaceTextView>(R.id.popup_app_info).onClick()
        contentView.findViewById<TypeFaceTextView>(R.id.popup_send).onClick()
    }

    private fun TextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString())
            dismiss()
        }
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}
