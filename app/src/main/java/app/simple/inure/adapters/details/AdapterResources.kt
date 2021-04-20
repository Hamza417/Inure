package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.viewholders.VerticalListViewHolder

class AdapterResources(val list: MutableList<String>) : RecyclerView.Adapter<AdapterResources.Holder>() {

    private lateinit var resourceCallbacks: ResourceCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_resources, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.xml.text = list[position]

        holder.xml.setOnClickListener {
            resourceCallbacks.onResourceClicked(list[position])
        }
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
    }
}