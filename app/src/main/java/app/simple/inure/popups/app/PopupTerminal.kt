package app.simple.inure.popups.app

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.extension.popup.PopupMenuCallback

class PopupTerminal(view: View) : BasePopupWindow() {

    private lateinit var menuCallbacks: PopupMenuCallback

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_terminal_menu, PopupLinearLayout(view.context))
        val context = contentView.context

        contentView.findViewById<DynamicRippleTextView>(R.id.terminal_menu_close).onClick(context.getString(R.string.close))
        contentView.findViewById<DynamicRippleTextView>(R.id.terminal_menu_kill).onClick(context.getString(R.string.kill))

        init(contentView, view)
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