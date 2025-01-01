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

class PopupInstallerChartEntry(
        view: View,
        entry: Entry?,
        labels: HashMap<String, String>,
        function: ((PieEntry) -> Unit)? = null) : BasePopupWindow() {

    private val textEntry: DynamicRippleTextView

    init {
        val container = PopupLinearLayout(view.context).apply {
            setPadding(0, 0, 0, 0)
        }

        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_chart, container, true)

        textEntry = contentView.findViewById(R.id.popup_entry)
        textEntry.text = labels[(entry as PieEntry).label]
            ?: contentView.context.getString(R.string.unknown)

        textEntry.setOnClickListener {
            function?.let { it1 ->
                it1(entry)
            }

            dismiss()
        }

        container.requestLayout()
        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }
}