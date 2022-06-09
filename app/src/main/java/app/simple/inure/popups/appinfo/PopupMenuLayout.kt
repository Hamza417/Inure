package app.simple.inure.popups.appinfo

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.AppInformationPreferences

class PopupMenuLayout(anchor: View) : BasePopupWindow() {

    private val horizontal: DynamicRippleTextView
    private val grid: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_menu_layout, PopupLinearLayout(anchor.context))

        horizontal = contentView.findViewById(R.id.popup_horizontal)
        grid = contentView.findViewById(R.id.popup_grid)

        horizontal.setOnClickListener {
            AppInformationPreferences.setMenuLayout(HORIZONTAL).also {
                dismiss()
            }
        }

        grid.setOnClickListener {
            AppInformationPreferences.setMenuLayout(GRID).also {
                dismiss()
            }
        }

        when (AppInformationPreferences.getMenuLayout()) {
            HORIZONTAL -> horizontal.isSelected = true
            GRID -> grid.isSelected = true
        }

        init(contentView, anchor)
    }

    companion object {
        const val HORIZONTAL = 1
        const val GRID = 2
    }
}