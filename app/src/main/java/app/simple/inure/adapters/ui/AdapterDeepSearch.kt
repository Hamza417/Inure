package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.models.Search
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.InfoStripUtils.setAppInfo
import com.bumptech.glide.Glide

class AdapterDeepSearch(private var deepSearchInfo: ArrayList<Search>, private var searchKeyword: String = "") : RecyclerView.Adapter<AdapterDeepSearch.Holder>() {

    private lateinit var adapterDeepSearchCallbacks: AdapterDeepSearchCallbacks
    var ignoreCasing = SearchPreferences.isCasingIgnored()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_deep_search, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = deepSearchInfo[position].packageInfo.packageName
        holder.icon.loadAppIcon(deepSearchInfo[position].packageInfo.packageName, deepSearchInfo[position].packageInfo.safeApplicationInfo.enabled)
        holder.name.text = deepSearchInfo[position].packageInfo.safeApplicationInfo.name
        holder.packageId.text = deepSearchInfo[position].packageInfo.packageName

        holder.name.setAppVisualStates(deepSearchInfo[position].packageInfo)
        holder.info.setAppInfo(deepSearchInfo[position].packageInfo)
        holder.setDeepInfo(deepSearchInfo[position])

        holder.container.setOnClickListener {
            adapterDeepSearchCallbacks.onAppClicked(deepSearchInfo[position].packageInfo, holder.icon)
        }

        if (searchKeyword.isNotEmpty()) {
            AdapterUtils.searchHighlighter(holder.name, searchKeyword, ignoreCasing)
            AdapterUtils.searchHighlighter(holder.packageId, searchKeyword, ignoreCasing)
        }

        holder.container.setOnLongClickListener {
            adapterDeepSearchCallbacks.onAppLongPressed(deepSearchInfo[position].packageInfo, holder.icon)
            true
        }

        holder.permissions.setOnClickListener {
            adapterDeepSearchCallbacks.onPermissionsClicked(deepSearchInfo[position].packageInfo)
        }

        holder.activities.setOnClickListener {
            adapterDeepSearchCallbacks.onActivitiesClicked(deepSearchInfo[position].packageInfo)
        }

        holder.services.setOnClickListener {
            adapterDeepSearchCallbacks.onServicesClicked(deepSearchInfo[position].packageInfo)
        }

        holder.receivers.setOnClickListener {
            adapterDeepSearchCallbacks.onReceiversClicked(deepSearchInfo[position].packageInfo)
        }

        holder.providers.setOnClickListener {
            adapterDeepSearchCallbacks.onProvidersClicked(deepSearchInfo[position].packageInfo)
        }

        holder.resources.setOnClickListener {
            adapterDeepSearchCallbacks.onResourcesClicked(deepSearchInfo[position].packageInfo)
        }
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        Glide.with(holder.icon).clear(holder.icon)
    }

    override fun getItemCount(): Int {
        return deepSearchInfo.size
    }

    @SuppressLint("SetTextI18n")
    private fun Holder.setDeepInfo(search: Search) {
        permissions.text = "${search.permissions} ${context.getString(R.string.permissions)}"
        activities.text = "${search.activities} ${context.getString(R.string.activities)}"
        services.text = "${search.services} ${context.getString(R.string.services)}"
        receivers.text = "${search.receivers} ${context.getString(R.string.receivers)}"
        providers.text = "${search.providers} ${context.getString(R.string.providers)}"
        resources.text = "${search.resources} ${context.getString(R.string.resources)}"
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterDeepSearchCallbacks) {
        this.adapterDeepSearchCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.info)
        val permissions: DynamicRippleTextView = itemView.findViewById(R.id.permissions)
        val activities: DynamicRippleTextView = itemView.findViewById(R.id.activities)
        val services: DynamicRippleTextView = itemView.findViewById(R.id.services)
        val receivers: DynamicRippleTextView = itemView.findViewById(R.id.receivers)
        val providers: DynamicRippleTextView = itemView.findViewById(R.id.providers)
        val resources: DynamicRippleTextView = itemView.findViewById(R.id.resources)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    companion object {
        interface AdapterDeepSearchCallbacks {
            fun onPermissionsClicked(packageInfo: PackageInfo)
            fun onActivitiesClicked(packageInfo: PackageInfo)
            fun onServicesClicked(packageInfo: PackageInfo)
            fun onReceiversClicked(packageInfo: PackageInfo)
            fun onProvidersClicked(packageInfo: PackageInfo)
            fun onResourcesClicked(packageInfo: PackageInfo)
            fun onAppClicked(packageInfo: PackageInfo, icon: AppIconImageView)
            fun onAppLongPressed(packageInfo: PackageInfo, icon: AppIconImageView)
        }
    }
}
