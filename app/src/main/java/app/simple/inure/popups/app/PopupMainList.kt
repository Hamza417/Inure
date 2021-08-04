package app.simple.inure.popups.app

import android.content.pm.ApplicationInfo
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.TypeFaceTextView

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
class PopupMainList(contentView: View, private val applicationInfo: ApplicationInfo, anchor: View) : BasePopupWindow() {

    lateinit var popupMenuCallback: PopupMenuCallback

    init {
        init(contentView, anchor, Gravity.END)

        contentView.findViewById<TypeFaceTextView>(R.id.popup_app_info).onClick()
        contentView.findViewById<TypeFaceTextView>(R.id.popup_send).onClick()
    }

    private fun TextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString(), applicationInfo)
            dismiss()
        }
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}
