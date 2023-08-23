package app.simple.inure.popups.configuration

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupShortcuts(view: View, onCreateShortcut: () -> Unit) : BasePopupWindow() {

    private val createShortcut: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(
                R.layout.popup_shortcuts,
                PopupLinearLayout(view.context), true)

        createShortcut = contentView.findViewById(R.id.popup_create_shortcut)

        createShortcut.setOnClickListener {
            onCreateShortcut()
            dismiss()
        }

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }
}