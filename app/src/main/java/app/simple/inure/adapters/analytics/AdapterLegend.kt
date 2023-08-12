package app.simple.inure.adapters.analytics

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerAccentColor
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.Utils
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.ColorUtils
import com.github.mikephil.charting.data.PieEntry

class AdapterLegend(private val pieEntries: ArrayList<PieEntry>,
                    private val colors: ArrayList<Int>,
                    private val function: ((PieEntry, Boolean) -> Unit)? = null) : RecyclerView.Adapter<AdapterLegend.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_pie_legend, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.color.backgroundTintList = ColorStateList.valueOf(colors[position])
        holder.label.text = pieEntries[position].label

        holder.container.background =
            Utils.getCustomRippleDrawable(holder.container.background,
                                          ColorUtils.lightenColor(colors[position], 0.25F))

        holder.container.setOnClickListener {
            function?.invoke(pieEntries[position], false)
        }

        holder.container.setOnLongClickListener {
            function?.invoke(pieEntries[position], true)
            true
        }
    }

    override fun getItemCount(): Int {
        return pieEntries.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val color: DynamicCornerAccentColor = itemView.findViewById(R.id.legend_color)
        val label: TypeFaceTextView = itemView.findViewById(R.id.legend_text)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }

    companion object {
        interface LegendCallbacks {
            fun onLegendClicked(pieEntry: PieEntry)
            fun onLegendLongPressed(pieEntry: PieEntry)
        }
    }
}