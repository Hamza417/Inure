package app.simple.inure.adapters.popups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.CheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.util.StringUtils.highlightExtensions

class AdapterGraphicsFilter : RecyclerView.Adapter<AdapterGraphicsFilter.Holder>() {

    private var isHighlighted = GraphicsPreferences.isExtensionsHighlighted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_extras_filter, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(holder) {
            extension.text = if (isHighlighted) list[position].first.toSpannable().highlightExtensions() else list[position].first
            checkBox.setChecked(GraphicsPreferences.isFilterAllowed(list[position].second))

            checkBox.setOnCheckedChangeListener { isChecked ->
                GraphicsPreferences.setFilterVisibility(isChecked, list[position].second)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val extension: TypeFaceTextView = itemView.findViewById(R.id.adapter_extras_filter_extension)
        val checkBox: CheckBox = itemView.findViewById(R.id.adapter_extras_filter_checkbox)
    }

    companion object {
        val list = arrayListOf(
                Pair(".png", GraphicsPreferences.png),
                Pair(".jpg", GraphicsPreferences.jpg),
                Pair(".jpeg", GraphicsPreferences.jpeg),
                Pair(".gif", GraphicsPreferences.gif),
                Pair(".webp", GraphicsPreferences.webp),
                Pair(".svg", GraphicsPreferences.svg)
        )
    }
}