package app.simple.inure.adapters.preferences

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.corners.DynamicCornerFrameLayout
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder

class AdapterColorPalette : RecyclerView.Adapter<AdapterColorPalette.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_color_palette, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.color.backgroundTintList = ColorStateList.valueOf(Colors.getColors()[position])
    }

    override fun getItemCount(): Int {
        return Colors.getColors().size
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val color: DynamicCornerFrameLayout = itemView.findViewById(R.id.color)
    }
}