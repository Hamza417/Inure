package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.Search
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.FileUtils.toFileOrNull
import app.simple.inure.util.InfoStripUtils.setAppInfo

class AdapterSearch(private var apps: ArrayList<Search>, private var searchKeyword: String = "") : RecyclerView.Adapter<AdapterSearch.Holder>() {

    private lateinit var adapterCallbacks: AdapterCallbacks
    var ignoreCasing = SearchPreferences.isCasingIgnored()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_all_apps_small_details, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = apps[position].packageInfo.packageName
        holder.icon.loadAppIcon(
                apps[position].packageInfo.packageName,
                apps[position].packageInfo.applicationInfo.enabled,
                apps[position].packageInfo.applicationInfo.sourceDir.toFileOrNull())
        holder.name.text = apps[position].packageInfo.applicationInfo.name
        holder.packageId.text = apps[position].packageInfo.packageName

        holder.name.setAppVisualStates(apps[position].packageInfo)
        holder.info.setAppInfo(apps[position].packageInfo)

        holder.container.setOnClickListener {
            adapterCallbacks.onAppClicked(apps[position].packageInfo, holder.icon)
        }

        if (searchKeyword.isNotEmpty()) {
            AdapterUtils.searchHighlighter(holder.name, searchKeyword, ignoreCasing)
            AdapterUtils.searchHighlighter(holder.packageId, searchKeyword, ignoreCasing)
        }

        holder.container.setOnLongClickListener {
            adapterCallbacks.onAppLongPressed(apps[position].packageInfo, holder.icon)
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
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.details)
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
    }
}
