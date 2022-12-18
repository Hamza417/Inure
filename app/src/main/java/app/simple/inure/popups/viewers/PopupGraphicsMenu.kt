package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.preferences.GraphicsPreferences

class PopupGraphicsMenu(view: View) : BasePopupWindow() {

    private var popupMenuCallback: PopupMenuCallback? = null
    private var highlightCheckBox: InureCheckBox

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_graphics_options, PopupLinearLayout(view.context))
        init(contentView, view)

        highlightCheckBox = contentView.findViewById(R.id.graphics_highlight_checkbox)

        highlightCheckBox.setChecked(GraphicsPreferences.isExtensionsHighlighted())

        highlightCheckBox.setOnCheckedChangeListener { isChecked ->
            GraphicsPreferences.setHighlightExtensions(isChecked)
        }
    }

    override fun dismiss() {
        super.dismiss()
        popupMenuCallback?.onDismiss()
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}
