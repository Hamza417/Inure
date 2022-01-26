package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterDeviceInfo(val list: List<Pair<Int, String>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterDeviceInfoCallbacks: AdapterDeviceInfoCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_device_info, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_device_info_panels, parent, false))
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            holder.icon.transitionName = list[position].second
            holder.icon.setImageResource(list[position].first)
            holder.text.text = list[position].second

            holder.container.setOnClickListener {
                adapterDeviceInfoCallbacks.onItemClicked(list[position].second, holder.icon)
            }
        } else if (holder is Header) {
            holder.search.setOnClickListener {
                adapterDeviceInfoCallbacks.onSearchClicked()
            }

            holder.settings.setOnClickListener {
                adapterDeviceInfoCallbacks.onSettingsClicked()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else {
            RecyclerViewConstants.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val text: TypeFaceTextView = itemView.findViewById(R.id.device_info_text)
        val icon: ImageView = itemView.findViewById(R.id.device_info_icon)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.device_info_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val search: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_search_button)
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
    }

    fun setOnDeviceInfoCallbackListener(deviceInfoCallbacks: AdapterDeviceInfoCallbacks) {
        this.adapterDeviceInfoCallbacks = deviceInfoCallbacks
    }

    companion object {
        interface AdapterDeviceInfoCallbacks {
            fun onItemClicked(source: String, icon: View)
            fun onSearchClicked()
            fun onSettingsClicked()
        }
    }
}