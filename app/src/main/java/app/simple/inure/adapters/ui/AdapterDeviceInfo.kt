package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.RecyclerViewUtils

class AdapterDeviceInfo(val list: List<Pair<Int, String>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterDeviceInfoCallbacks: AdapterDeviceInfoCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_device_info, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_device_info_panels, parent, false))
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, originalPosition: Int) {
        val position = originalPosition - 1

        if (holder is Holder) {
            holder.icon.transitionName = list[position].second
            holder.icon.setImageResource(list[position].first)
            holder.text.text = list[position].second

            holder.container.setOnClickListener {
                adapterDeviceInfoCallbacks.onItemClicked(list[position].second, holder.icon)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val text: TypeFaceTextView = itemView.findViewById(R.id.text)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView)

    fun setOnDeviceInfoCallbackListener(deviceInfoCallbacks: AdapterDeviceInfoCallbacks) {
        this.adapterDeviceInfoCallbacks = deviceInfoCallbacks
    }

    companion object {
        interface AdapterDeviceInfoCallbacks {
            fun onItemClicked(source: String, icon: View)
        }
    }
}