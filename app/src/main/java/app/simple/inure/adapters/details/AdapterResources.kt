package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.optimizeToColoredString

class AdapterResources(private val list: MutableList<String>, private val keyword: String) : RecyclerView.Adapter<AdapterResources.Holder>() {

    private var resourceCallbacks: ResourceCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_resources, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.xml.text = list[position].optimizeToColoredString("/")
        list[position].optimizeToColoredString("...") // fade ellipsis maybe?

        holder.xml.setOnClickListener {
            resourceCallbacks?.onResourceClicked(list[position])
        }

        holder.xml.setOnLongClickListener {
            resourceCallbacks?.onResourceLongClicked(list[position])
            true
        }

        if (keyword.isNotBlank()) AdapterUtils.searchHighlighter(holder.xml, keyword)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnResourceClickListener(resourceCallbacks: ResourceCallbacks) {
        this.resourceCallbacks = resourceCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val xml: DynamicRippleTextView = itemView.findViewById(R.id.adapter_resources_name)
    }

    interface ResourceCallbacks {
        fun onResourceClicked(path: String)
        fun onResourceLongClicked(path: String)
    }
}