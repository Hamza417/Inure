package app.simple.inure.popups.app

import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class PopupTerminal(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var menuCallbacks: PopupMenuCallback

    init {
        init(contentView, view)

        val context = contentView.context

        contentView.findViewById<DynamicRippleTextView>(R.id.terminal_menu_close).onClick(context.getString(R.string.close))
        contentView.findViewById<DynamicRippleTextView>(R.id.terminal_menu_kill).onClick(context.getString(R.string.kill))
    }

    private fun DynamicRippleTextView.onClick(string: String) {
        this.setOnClickListener {
            dismiss()
            menuCallbacks.onMenuItemClicked(string)
        }
    }

    fun setOnMenuClickListener(popupMainMenuCallbacks: PopupMenuCallback) {
        this.menuCallbacks = popupMainMenuCallbacks
    }
}