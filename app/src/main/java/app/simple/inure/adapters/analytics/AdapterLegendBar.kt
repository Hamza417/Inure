package app.simple.inure.adapters.analytics

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
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry

class AdapterLegendBar(private val barEntries: ArrayList<BarEntry>,
                       private val colors: ArrayList<Int>,
                       private val function: ((BarEntry, Boolean) -> Unit)? = null) : RecyclerView.Adapter<AdapterLegendBar.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_legend, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.color.backgroundTintList = ColorStateList.valueOf(colors[position])
        holder.label.text = barEntries[position].data.toString()

        holder.container.setRippleColor(
                ColorUtils.lightenColor(colors[position], 0.2F))

        holder.container.setOnClickListener {
            function?.invoke(barEntries[position], false)
        }

        holder.container.setOnLongClickListener {
            function?.invoke(barEntries[position], true)
            true
        }
    }

    override fun getItemCount(): Int {
        return barEntries.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val color: DynamicCornerAccentColor = itemView.findViewById(R.id.legend_color)
        val label: TypeFaceTextView = itemView.findViewById(R.id.legend_text)
        val container: DynamicRippleLegendLinearLayout = itemView.findViewById(R.id.container)
    }

    companion object {
        interface LegendCallbacks {
            fun onLegendClicked(pieEntry: PieEntry)
            fun onLegendLongPressed(pieEntry: PieEntry)
        }
    }
}