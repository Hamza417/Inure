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

        holder.data.apply {
            text = StringBuilder().apply {
                append("↑")
                append(data[position].dataSent.getFileSize())
                append(" ")
                append("↓")
                append(data[position].dataReceived.getFileSize())
            }
        }

        holder.wifi.apply {
            text = StringBuilder().apply {
                append("↑")
                append(data[position].dataSentWifi.getFileSize())
                append(" ")
                append("↓")
                append(data[position].dataReceivedWifi.getFileSize())
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val time: TypeFaceTextView = itemView.findViewById(R.id.total_time_used)
        val data: TypeFaceTextView = itemView.findViewById(R.id.total_data_used)
        val wifi: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_used)
    }
}