package app.simple.inure.adapters.deviceinfo

import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterDeviceInfoContent(val arrayList: ArrayList<Pair<String, Spannable>>) : RecyclerView.Adapter<AdapterDeviceInfoContent.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_device_info_items, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.heading.text = arrayList[position].first
        holder.data.text = arrayList[position].second
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_device_info_container)
        val heading: TypeFaceTextView = itemView.findViewById(R.id.device_info_heading)
        val data: TypeFaceTextView = itemView.findViewById(R.id.device_info_data)
    }
}