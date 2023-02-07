package app.simple.inure.popups.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.GeneratedDataPreferences

class PopupGeneratedDataFormat(view: View) : BasePopupWindow() {

    private val txt: DynamicRippleTextView
    private val md: DynamicRippleTextView
    private val html: DynamicRippleTextView
    private val csv: DynamicRippleTextView
    private val json: DynamicRippleTextView
    private val xml: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_data_format, PopupLinearLayout(view.context))

        txt = contentView.findViewById(R.id.txt)
        md = contentView.findViewById(R.id.md)
        html = contentView.findViewById(R.id.html)
        csv = contentView.findViewById(R.id.csv)
        json = contentView.findViewById(R.id.json)
        xml = contentView.findViewById(R.id.xml)

        txt.onClick(GeneratedDataPreferences.TXT)
        md.onClick(GeneratedDataPreferences.MD)
        html.onClick(GeneratedDataPreferences.HTML)
        csv.onClick(GeneratedDataPreferences.CSV)
        json.onClick(GeneratedDataPreferences.JSON)
        xml.onClick(GeneratedDataPreferences.XML)

        when (GeneratedDataPreferences.getGeneratedDataType()) {
            GeneratedDataPreferences.TXT -> txt.isSelected = true
            GeneratedDataPreferences.MD -> md.isSelected = true
            GeneratedDataPreferences.HTML -> html.isSelected = true
            GeneratedDataPreferences.CSV -> csv.isSelected = true
            GeneratedDataPreferences.JSON -> json.isSelected = true
            GeneratedDataPreferences.XML -> xml.isSelected = true
        }

        init(contentView, view, Gravity.START)
    }

    private fun TextView.onClick(category: String) {
        this.setOnClickListener {
            GeneratedDataPreferences.setGeneratedDataType(category)
            dismiss()
        }
    }
}