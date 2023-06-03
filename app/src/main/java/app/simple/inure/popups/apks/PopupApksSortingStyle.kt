package app.simple.inure.popups.apks

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.SortApks

class PopupApksSortingStyle(view: View) : BasePopupWindow() {

    private val name: DynamicRippleTextView
    private val size: DynamicRippleTextView
    private val date: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_apks_sorting_style, PopupLinearLayout(view.context))
        init(contentView, view)

        name = contentView.findViewById(R.id.name)
        size = contentView.findViewById(R.id.size)
        date = contentView.findViewById(R.id.date)
        val reversedCheckBox = contentView.findViewById<InureCheckBox>(R.id.checkbox)

        name.onClick(SortApks.NAME)
        size.onClick(SortApks.SIZE)
        date.onClick(SortApks.DATE)

        when (ApkBrowserPreferences.getSortStyle()) {
            SortApks.NAME -> name.isSelected = true
            SortApks.SIZE -> size.isSelected = true
            SortApks.DATE -> date.isSelected = true
        }

        reversedCheckBox.setChecked(ApkBrowserPreferences.isReverseSorting())

        contentView.findViewById<DynamicRippleTextView>(R.id.reversed).setOnClickListener {
            reversedCheckBox.toggle()
        }

        reversedCheckBox.setOnCheckedChangeListener { isChecked ->
            ApkBrowserPreferences.setReverseSorting(isChecked)
        }
    }

    private fun DynamicRippleTextView.onClick(sort: String) {
        this.setOnClickListener {
            ApkBrowserPreferences.setSortStyle(sort)
            dismiss()
        }
    }
}