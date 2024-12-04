package app.simple.inure.popups.appearances

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.AppearancePreferences

class PopupLayoutStyle(view: View) : BasePopupWindow() {

    private val normal: DynamicRippleTextView
    private val condensed: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_layout_style, PopupLinearLayout(view.context), true)

        normal = contentView.findViewById(R.id.normal)
        condensed = contentView.findViewById(R.id.condensed)

        normal.onClick(AppearancePreferences.LIST_STYLE_NORMAL)
        condensed.onClick(AppearancePreferences.LIST_STYLE_CONDENSED)

        when (AppearancePreferences.getListStyle()) {
            AppearancePreferences.LIST_STYLE_NORMAL -> normal.isSelected = true
            AppearancePreferences.LIST_STYLE_CONDENSED -> condensed.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(style: Int) {
        this.setOnClickListener {
            AppearancePreferences.setListStyle(style)
            dismiss()
        }
    }
}
