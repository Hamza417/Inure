package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.model.PackageStats
import app.simple.inure.util.FileSizeHelper.getFileSize

class StatisticsAdapter(private val data: ArrayList<PackageStats>) : RecyclerView.Adapter<StatisticsAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_usage_stats, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.loadAppIcon(data[position].packageInfo!!.packageName)
        holder.name.text = data[position].packageInfo!!.applicationInfo.name

        holder.dataUp.text = data[position].dataSent.getFileSize()
        holder.dataDown.text = data[position].dataReceived.getFileSize()
        holder.wifiUp.text = data[position].dataSentWifi.getFileSize()
        holder.wifiDown.text = data[position].dataReceivedWifi.getFileSize()
    }

    override fun getItemCount(): Int {
        return data.size
    }


    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val time: TypeFaceTextView = itemView.findViewById(R.id.total_time_used)
        val dataUp: TypeFaceTextView = itemView.findViewById(R.id.total_data_up_used)
        val dataDown: TypeFaceTextView = itemView.findViewById(R.id.total_data_down_used)
        val wifiUp: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_up_used)
        val wifiDown: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_down_used)
    }
}