package app.simple.inure.adapters.deviceinfo

import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterDeviceInfoContent(val arrayList: ArrayList<Pair<String, Spannable>>, private val heading: String) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_device_info_heading, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_device_info_items, parent, false))
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_.minus(1)

        if (holder is Holder) {
            holder.title.text = arrayList[position].first
            holder.data.text = arrayList[position].second
        } else if (holder is Header) {
            holder.heading.text = heading
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else {
            RecyclerViewConstants.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_device_info_container)
        val title: TypeFaceTextView = itemView.findViewById(R.id.device_info_title)
        val data: TypeFaceTextView = itemView.findViewById(R.id.device_info_data)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val heading: TypeFaceTextView = itemView.findViewById(R.id.device_info_heading)
    }
}