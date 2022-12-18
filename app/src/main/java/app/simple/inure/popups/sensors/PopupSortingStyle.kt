package app.simple.inure.popups.sensors

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.SensorsPreferences
import app.simple.inure.util.SortSensors

class PopupSortingStyle(view: View) : BasePopupWindow() {

    private val name: DynamicRippleTextView
    private val power: DynamicRippleTextView
    private val maximumRange: DynamicRippleTextView
    private val resolution: DynamicRippleTextView

    private val descCheckBox: InureCheckBox

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_sensors_sort, PopupLinearLayout(view.context))

        name = contentView.findViewById(R.id.sort_name)
        power = contentView.findViewById(R.id.sort_power)
        maximumRange = contentView.findViewById(R.id.sort_max_range)
        resolution = contentView.findViewById(R.id.sort_res)
        descCheckBox = contentView.findViewById(R.id.sort_reversed_checkbox)

        when (SensorsPreferences.getSortStyle()) {
            SortSensors.NAME -> name.isSelected = true
            SortSensors.POWER -> power.isSelected = true
            SortSensors.MAX_RANGE -> maximumRange.isSelected = true
            SortSensors.RESOLUTION -> resolution.isSelected = true
        }

        descCheckBox.setChecked(SensorsPreferences.isReverseSorting())

        name.onClick(SortSensors.NAME)
        power.onClick(SortSensors.POWER)
        maximumRange.onClick(SortSensors.MAX_RANGE)
        resolution.onClick(SortSensors.RESOLUTION)

        descCheckBox.setOnCheckedChangeListener { isChecked ->
            SensorsPreferences.setReverseSorting(isChecked)
        }

        init(contentView, view)
    }

    private fun TextView.onClick(style: String) {
        this.setOnClickListener {
            SensorsPreferences.setSortStyle(style)
            dismiss()
        }
    }
}