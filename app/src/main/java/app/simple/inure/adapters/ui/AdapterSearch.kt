package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.PackageListUtils.setAppInfo

class AdapterSearch(private var apps: ArrayList<PackageInfo>, private var searchKeyword: String = "") : RecyclerView.Adapter<AdapterSearch.Holder>() {

    private lateinit var adapterCallbacks: AdapterCallbacks
    var ignoreCasing = SearchPreferences.isCasingIgnored()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_all_apps_small_details, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = apps[position].packageName
        holder.icon.loadAppIcon(apps[position].packageName, apps[position].applicationInfo.enabled)
        holder.name.text = apps[position].applicationInfo.name
        holder.packageId.text = apps[position].packageName

        holder.name.setStrikeThru(apps[position].applicationInfo.enabled)
        holder.info.setAppInfo(apps[position])

        holder.container.setOnClickListener {
            adapterCallbacks.onAppClicked(apps[position], holder.icon)
        }

        if (searchKeyword.isNotEmpty()) {
            AdapterUtils.searchHighlighter(holder.name, searchKeyword, ignoreCasing)
            AdapterUtils.searchHighlighter(holder.packageId, searchKeyword, ignoreCasing)
        }

        holder.container.setOnLongClickListener {
            adapterCallbacks.onAppLongPressed(apps[position], holder.icon)
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

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_all_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_all_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.adapter_all_app_info)
        val container: ConstraintLayout = itemView.findViewById(R.id.adapter_all_app_container)
    }
}
