package app.simple.inure.popups.appearances

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.AccessibilityPreferences

class PopupPalettes(view: View) : BasePopupWindow() {

    private val pastel: DynamicRippleTextView
    private val retro: DynamicRippleTextView
    private val coffee: DynamicRippleTextView
    private val cold: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_palette, PopupLinearLayout(view.context), true)

        pastel = contentView.findViewById(R.id.pastel)
        retro = contentView.findViewById(R.id.retro)
        coffee = contentView.findViewById(R.id.coffee)
        cold = contentView.findViewById(R.id.cold)

        pastel.onClick(Colors.PASTEL)
        retro.onClick(Colors.RETRO)
        coffee.onClick(Colors.COFFEE)
        cold.onClick(Colors.COLD)

        when (AccessibilityPreferences.getColorfulIconsPalette()) {
            Colors.PASTEL -> pastel.isSelected = true
            Colors.RETRO -> retro.isSelected = true
            Colors.COFFEE -> coffee.isSelected = true
            Colors.COLD -> cold.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(palette: Int) {
        this.setOnClickListener {
            AccessibilityPreferences.setColorfulIconsPalette(palette)
            dismiss()
        }
    }
}