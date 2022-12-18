package app.simple.inure.adapters.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.RecyclerViewUtils

class AdapterDevelopmentPreferences : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_development_preferences, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_development_preferences, parent, false))
            }
            else -> {
                // This should never happen
                throw IllegalStateException("Unknown view type $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        @Suppress("NAME_SHADOWING")
        val position = position - 1

        if (holder is Holder) {
            holder.title.text = DevelopmentPreferences.developmentPreferences[position].title
            holder.description.text = DevelopmentPreferences.developmentPreferences[position].description

            holder.checkBox.setCheckedWithoutAnimations(DevelopmentPreferences
                                                            .get(DevelopmentPreferences.developmentPreferences[position].key))

            holder.checkBox.setOnCheckedChangeListener {
                DevelopmentPreferences.set(DevelopmentPreferences.developmentPreferences[position].key, it)
            }

            holder.container.setOnClickListener {
                holder.checkBox.toggle()
            }
        }
    }

    override fun getItemCount(): Int {
        return DevelopmentPreferences.developmentPreferences.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView)

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_development_preferences_container)
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_development_preferences_title)
        val description: TypeFaceTextView = itemView.findViewById(R.id.adapter_development_preferences_description)
        val checkBox: InureCheckBox = itemView.findViewById(R.id.adapter_development_preferences_checkbox)
    }
}