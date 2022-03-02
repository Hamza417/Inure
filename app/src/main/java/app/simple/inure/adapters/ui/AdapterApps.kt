package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.util.AdapterUtils.searchHighlighter

@Deprecated("not in use anymore")
class AdapterApps : RecyclerView.Adapter<AdapterApps.Holder>(), PopupTextProvider {

    private lateinit var appsAdapterCallbacks: AppsAdapterCallbacks
    var apps = arrayListOf<PackageInfo>()
    var searchKeyword: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_all_apps, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = "app_$position"
        holder.icon.loadAppIcon(apps[position].packageName)
        holder.name.text = apps[position].applicationInfo.name
        holder.packageId.text = apps[position].packageName

        if (searchKeyword.isNotEmpty()) {
            searchHighlighter(holder.name, searchKeyword)
            searchHighlighter(holder.packageId, searchKeyword)
        }

        holder.container.setOnClickListener {
            appsAdapterCallbacks.onAppClicked(apps[position], holder.icon)
        }

        holder.container.setOnLongClickListener {
            appsAdapterCallbacks.onAppLongPressed(apps[position], holder.icon)
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

    fun setOnItemClickListener(appsAdapterCallbacks: AppsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_all_app_icon)
        val name: TextView = itemView.findViewById(R.id.adapter_all_app_name)
        val packageId: TextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val container: ConstraintLayout = itemView.findViewById(R.id.adapter_all_app_container)
    }

    override fun getPopupText(position: Int): String {
        return apps[position].applicationInfo.name.substring(0, 1)
    }
}
