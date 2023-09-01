package app.simple.inure.adapters.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.glide.util.ImageLoader.loadAppIcon

class AdapterTaggedIcons(private val packageNames: List<String>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when ((parent as RecyclerView).layoutManager) {
            is GridLayoutManager -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_tag_icons, parent, false))
            }
            is LinearLayoutManager -> {
                HolderLinear(LayoutInflater.from(parent.context)
                                 .inflate(R.layout.adapter_tag_icons_linear, parent, false))
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Holder) {
            holder.icon.loadAppIcon(packageNames[position], true)
        } else if (holder is HolderLinear) {
            holder.icon.loadAppIcon(packageNames[position], true)
        }
    }

    override fun getItemCount(): Int {
        return packageNames.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
    }

    inner class HolderLinear(itemView: View) : HorizontalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
    }
}