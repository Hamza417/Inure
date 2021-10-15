package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.model.PackageStats
import app.simple.inure.util.FileSizeHelper.toSize
import java.util.concurrent.TimeUnit

class StatisticsAdapter : RecyclerView.Adapter<VerticalListViewHolder>(), PopupTextProvider {

    private var data = arrayListOf<PackageStats>()
    private var statsAdapterCallbacks: StatsAdapterCallbacks? = null
    private var isLoaded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            VerticalListViewHolder.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_usage_stats_header, parent, false))
            }
            VerticalListViewHolder.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_usage_stats, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            holder.icon.transitionName = "stats_app_$position"
            holder.icon.loadAppIcon(data[position].packageInfo!!.packageName)
            holder.name.text = data[position].packageInfo!!.applicationInfo.name
            holder.dataUp.text = data[position].dataSent.toSize()
            holder.dataDown.text = data[position].dataReceived.toSize()
            holder.wifiUp.text = data[position].dataSentWifi.toSize()
            holder.wifiDown.text = data[position].dataReceivedWifi.toSize()

            with(data[position].totalTimeUsed) {
                holder.time.apply {
                    this.text = when {
                        TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_seconds,
                                                   TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_short,
                                                   TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                        }
                        else -> {
                            this.context.getString(R.string.used_for_long,
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                    }
                }
            }

            holder.container.setOnClickListener {
                statsAdapterCallbacks?.onAppClicked(data[position].packageInfo!!, holder.icon)
            }

            holder.container.setOnLongClickListener {
                statsAdapterCallbacks?.onAppLongClicked(data[position].packageInfo!!, holder.icon, holder.container)
                true
            }

        } else if (holder is Header) {
            holder.settings.setOnClickListener {
                statsAdapterCallbacks?.onFilterPressed(it)
            }

            if (isLoaded) {
                holder.progress.hide()
            } else {
                holder.progress.show()
            }

            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), data.size)
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VerticalListViewHolder.TYPE_HEADER
        } else VerticalListViewHolder.TYPE_ITEM
    }

    override fun getPopupText(position: Int): String {
        return data[position].packageInfo?.applicationInfo?.name?.substring(0, 1) ?: ""
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: ArrayList<PackageStats>) {
        this.data = list
        notifyDataSetChanged()
        isLoaded = true
    }

    fun setOnStatsCallbackListener(statsAdapterCallbacks: StatsAdapterCallbacks) {
        this.statsAdapterCallbacks = statsAdapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_usage_stats_container)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val time: TypeFaceTextView = itemView.findViewById(R.id.total_time_used)
        val dataUp: TypeFaceTextView = itemView.findViewById(R.id.total_data_up_used)
        val dataDown: TypeFaceTextView = itemView.findViewById(R.id.total_data_down_used)
        val wifiUp: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_up_used)
        val wifiDown: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_down_used)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val progress: ContentLoadingProgressBar = itemView.findViewById(R.id.progress)
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
    }

    companion object {
        interface StatsAdapterCallbacks {
            fun onFilterPressed(view: View)
            fun onAppClicked(packageInfo: PackageInfo, icon: ImageView)
            fun onAppLongClicked(packageInfo: PackageInfo, icon: ImageView, anchor: ViewGroup)
        }
    }
}