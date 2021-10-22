package app.simple.inure.adapters.home

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks

class AdapterRecentlyUpdated : RecyclerView.Adapter<AdapterRecentlyUpdated.Holder>() {

    var apps = arrayListOf<PackageInfo>()
    private lateinit var appsAdapterCallbacks: AppsAdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                              .inflate(R.layout.adapter_recently_installed, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = "recently_app_$position"
        holder.icon.loadAppIcon(apps[position].packageName)
        holder.name.text = apps[position].applicationInfo.name
        holder.packageId.text = apps[position].packageName

        if (apps[position].applicationInfo.enabled) {
            holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        } else {
            holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        holder.date.text = apps[position].getApplicationLastUpdateTime(holder.itemView.context)

        holder.container.setOnClickListener {
            appsAdapterCallbacks.onAppClicked(apps[position], holder.icon)
        }

        holder.container.setOnLongClickListener {
            appsAdapterCallbacks.onAppLongPress(apps[position], it, holder.icon, position)
            true
        }
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        GlideApp.with(holder.icon).clear(holder.icon)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnItemClickListener(appsAdapterCallbacks: AppsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_recently_app_icon)
        val name: TextView = itemView.findViewById(R.id.adapter_recently_app_name)
        val packageId: TextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val date: TextView = itemView.findViewById(R.id.adapter_recently_date)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_recently_container)
    }
}