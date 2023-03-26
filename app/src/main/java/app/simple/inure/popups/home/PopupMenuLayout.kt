package app.simple.inure.popups.home

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.HomePreferences

class PopupMenuLayout(anchor: View) : BasePopupWindow() {

    private val horizontal: DynamicRippleTextView
    private val grid: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_home_menu_layout, PopupLinearLayout(anchor.context))

        horizontal = contentView.findViewById(R.id.popup_vertical)
        grid = contentView.findViewById(R.id.popup_grid)

        horizontal.setOnClickListener {
            HomePreferences.setMenuLayout(VERTICAL).also {
                dismiss()
            }
        }

        grid.setOnClickListener {
            HomePreferences.setMenuLayout(GRID).also {
                dismiss()
            }
        }

        when (HomePreferences.getMenuLayout()) {
            VERTICAL -> horizontal.isSelected = true
            GRID -> grid.isSelected = true
        }

        init(contentView, anchor)
    }

    companion object {
        const val VERTICAL = 1
        const val GRID = 2
    }
}