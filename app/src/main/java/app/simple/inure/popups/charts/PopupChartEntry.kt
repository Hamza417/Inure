package app.simple.inure.popups.charts

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry

class PopupChartEntry(view: View, entry: Entry?, function: ((PieEntry) -> Unit)? = null) : BasePopupWindow() {

    private val textEntry: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_chart,
                     PopupLinearLayout(view.context).apply {
                         setPadding(0, 0, 0, 0)
                     },
                     true)

        textEntry = contentView.findViewById(R.id.popup_entry)

        init(contentView, view, Misc.xOffset, Misc.yOffset)

        textEntry.text = (entry as PieEntry).label

        textEntry.setOnClickListener {
            function?.let { it1 -> it1(entry) }
            dismiss()
        }
    }
}