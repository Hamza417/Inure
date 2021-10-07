package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.preferences.ExtrasPreferences

class PopupExtrasMenu(view: View) : BasePopupWindow() {

    private var popupMenuCallback: PopupMenuCallback? = null
    private var highlightCheckBox: CheckBox

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_extras_options, PopupLinearLayout(view.context))
        init(contentView, view)

        highlightCheckBox = contentView.findViewById(R.id.extras_highlight_checkbox)

        highlightCheckBox.isChecked = ExtrasPreferences.isExtensionsHighlighted()

        highlightCheckBox.setOnCheckedChangeListener { _, isChecked ->
            ExtrasPreferences.setHighlightExtensions(isChecked)
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
