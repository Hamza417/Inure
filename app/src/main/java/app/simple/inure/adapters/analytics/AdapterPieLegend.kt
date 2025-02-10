package app.simple.inure.adapters.analytics

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerAccentColor
import app.simple.inure.decorations.ripple.DynamicRippleLegendLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.NullSafety.isNotNull
import com.github.mikephil.charting.data.PieEntry
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class AdapterPieLegend(private val pieEntries: ArrayList<PieEntry>,
                       private val colors: ArrayList<Int>,
                       private val function: ((PieEntry, Boolean) -> Unit)? = null) : RecyclerView.Adapter<AdapterPieLegend.Holder>() {

    private var highLightedEntry: PieEntry? = null
    private var lastHighlightedEntry: PieEntry? = null
    private val decimalFormat = DecimalFormat("#0.0")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_legend, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.color.backgroundTintList = ColorStateList.valueOf(colors[position])

        val percent = pieEntries[position].value / pieEntries.sumOf { it.value.toInt() }.toFloat() * 100F
        val percentFormatted = decimalFormat.format(percent)
        holder.label.text = buildString {
            append(pieEntries[position].label)
            append(" (")
            append("$percentFormatted%")
            append(")")
        }

        holder.container.setOnClickListener {
            function?.invoke(pieEntries[position], false)
        }

        holder.container.setOnLongClickListener {
            function?.invoke(pieEntries[position], true)
            true
        }

        if (highLightedEntry != null && highLightedEntry!!.label == pieEntries[position].label) {
            holder.container.highlight(ColorUtils.lightenColor(colors[position], 0.2F))
        } else {
            holder.container.setRippleColor(ColorUtils.lightenColor(colors[position], 0.2F))
            holder.container.unHighlight()
        }
    }

    override fun getItemCount(): Int {
        return pieEntries.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun highlightEntry(e: PieEntry?) {
        highLightedEntry = e
        if (e.isNotNull()) {
            for (i in pieEntries.indices) {
                if (pieEntries[i].label == e?.label) {
                    lastHighlightedEntry = e
                    notifyItemChanged(i)
                    break
                }
            }
        } else {
            for (i in pieEntries.indices) {
                if (pieEntries[i].label == lastHighlightedEntry?.label) {
                    lastHighlightedEntry = null
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val color: DynamicCornerAccentColor = itemView.findViewById(R.id.legend_color)
        val label: TypeFaceTextView = itemView.findViewById(R.id.legend_text)
        val container: DynamicRippleLegendLinearLayout = itemView.findViewById(R.id.container)
    }
}