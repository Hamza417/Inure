package app.simple.inure.adapters.viewers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.condensed.CondensedDynamicRippleTextView
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.models.DexClass
import app.simple.inure.util.AdapterUtils

class AdapterDexData(private val dexs: ArrayList<DexClass>, val keyword: String) : RecyclerView.Adapter<AdapterDexData.Holder>() {

    var onDetailsClicked: ((String) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_resources, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = dexs[position].className
        holder.name.setTrackingIcon(dexs[position].isTracker)

        if (dexs[position].isTracker) {
            AdapterUtils.searchHighlighter(holder.name, dexs[position].trackerSignature, ignoreCasing = true)
        } else {
            AdapterUtils.searchHighlighter(holder.name, keyword, ignoreCasing = true)
        }

        holder.name.setOnClickListener {
            onDetailsClicked?.invoke(dexs[position].className)
        }
    }

    override fun getItemCount(): Int {
        return dexs.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: CondensedDynamicRippleTextView = itemView.findViewById(R.id.adapter_resources_name)
    }
}
