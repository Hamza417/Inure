package app.simple.inure.adapters.home

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon

class AdapterQuickApps(private val list: ArrayList<PackageInfo>) : RecyclerView.Adapter<AdapterQuickApps.Holder>() {

    private var quickAppsAdapterCallbacks: QuickAppsAdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_recently_updated, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = list[position].packageName
        holder.icon.loadAppIcon(list[position].packageName, list[position].applicationInfo.enabled)
        holder.name.text = list[position].applicationInfo.name

        holder.name.setStrikeThru(list[position].applicationInfo.enabled)

        holder.container.setOnClickListener {
            quickAppsAdapterCallbacks?.onQuickAppClicked(list[position], holder.icon)
        }

        holder.container.setOnLongClickListener {
            quickAppsAdapterCallbacks?.onQuickAppLongClicked(list[position], holder.icon, holder.container)
            true
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.recently_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.recently_app_name)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.recently_container)
    }

    fun seyOnQuickAppAdapterCallbackListener(recentlyUpdatedAppsCallbacks: QuickAppsAdapterCallbacks) {
        this.quickAppsAdapterCallbacks = recentlyUpdatedAppsCallbacks
    }

    companion object {
        interface QuickAppsAdapterCallbacks {
            fun onQuickAppClicked(packageInfo: PackageInfo, icon: ImageView)
            fun onQuickAppLongClicked(packageInfo: PackageInfo, icon: ImageView, anchor: ViewGroup)
        }
    }
}