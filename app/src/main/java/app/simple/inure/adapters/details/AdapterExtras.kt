package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.util.StringUtils.optimizeToColoredString


class AdapterExtras(val list: MutableList<String>) : RecyclerView.Adapter<AdapterExtras.Holder>() {

    private lateinit var extrasCallbacks: ExtrasCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_resources, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.extra.text = list[position].optimizeToColoredString(holder.itemView.context, "/")

        holder.extra.setOnClickListener {
            extrasCallbacks.onExtrasClicked(list[position])
        }

        holder.extra.setOnLongClickListener {
            extrasCallbacks.onExtrasLongClicked(list[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnResourceClickListener(resourceCallbacks: ExtrasCallbacks) {
        this.extrasCallbacks = resourceCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val extra: DynamicRippleTextView = itemView.findViewById(R.id.adapter_resources_name)
    }

    interface ExtrasCallbacks {
        fun onExtrasClicked(path: String)
        fun onExtrasLongClicked(path: String)
    }
}