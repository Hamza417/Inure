package app.simple.inure.popups.battery

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort

class PopupBatteryOptimizationSortingStyle(view: View) : BasePopupWindow() {

    private val name: DynamicRippleTextView
    private val packageName: DynamicRippleTextView
    private val size: DynamicRippleTextView
    private val installDate: DynamicRippleTextView
    private val reversedCheckBox: InureCheckBox

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_sorting_style, PopupLinearLayout(view.context))
        init(contentView, view)

        name = contentView.findViewById(R.id.sort_name)
        packageName = contentView.findViewById(R.id.sort_package_name)
        size = contentView.findViewById(R.id.sort_app_size)
        installDate = contentView.findViewById(R.id.sort_install_date)
        reversedCheckBox = contentView.findViewById(R.id.sort_reversed_checkbox)

        name.onClick(Sort.NAME)
        packageName.onClick(Sort.PACKAGE_NAME)
        size.onClick(Sort.SIZE)
        installDate.onClick(Sort.INSTALL_DATE)

        when (BatteryOptimizationPreferences.getBatteryOptimizationSortStyle()) {
            Sort.NAME -> name.isSelected = true
            Sort.INSTALL_DATE -> installDate.isSelected = true
            Sort.SIZE -> size.isSelected = true
            Sort.PACKAGE_NAME -> packageName.isSelected = true
        }

        reversedCheckBox.setChecked(MainPreferences.isReverseSorting())

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_reversed).setOnClickListener {
            reversedCheckBox.toggle()
        }

        reversedCheckBox.setOnCheckedChangeListener { isChecked ->
            BatteryOptimizationPreferences.setBatteryOptimizationIsSortingReversed(isChecked)
        }
    }

    private fun TextView.onClick(style: String) {
        this.setOnClickListener {
            BatteryOptimizationPreferences.setBatteryOptimizationSortStyle(style)
            dismiss()
        }
    }
}