package app.simple.inure.adapters.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.glide.util.ImageLoader.loadAppIcon

class AdapterTaggedIcons(private val packageNames: List<String>) : RecyclerView.Adapter<AdapterTaggedIcons.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_tag_app, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.loadAppIcon(packageNames[position], true)
    }

    override fun getItemCount(): Int {
        return packageNames.size.coerceAtMost(4 * 6) // Four rows and six columns
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
    }
}