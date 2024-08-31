package app.simple.inure.adapters.preferences

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder

class AdapterPickedColors(private val colors: MutableSet<String>, private val onColorClicked: (String) -> Unit)
    : RecyclerView.Adapter<AdapterPickedColors.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_picked_colors, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.color.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colors.elementAt(position)))

        holder.color.setOnClickListener {
            onColorClicked(colors.elementAt(position))
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val color: View = itemView.findViewById(R.id.color)
    }
}
