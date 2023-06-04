package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.models.SearchModel
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.PackageListUtils.setAppInfo

class AdapterDeepSearch(private var deepSearchInfo: ArrayList<SearchModel>, private var searchKeyword: String = "") : RecyclerView.Adapter<AdapterDeepSearch.Holder>() {

    private lateinit var adapterDeepSearchCallbacks: AdapterDeepSearchCallbacks
    var ignoreCasing = SearchPreferences.isCasingIgnored()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_deep_search, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = deepSearchInfo[position].packageInfo.packageName
        holder.icon.loadAppIcon(deepSearchInfo[position].packageInfo.packageName, deepSearchInfo[position].packageInfo.applicationInfo.enabled)
        holder.name.text = deepSearchInfo[position].packageInfo.applicationInfo.name
        holder.packageId.text = deepSearchInfo[position].packageInfo.packageName

        holder.name.setStrikeThru(deepSearchInfo[position].packageInfo.applicationInfo.enabled)
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
        GlideApp.with(holder.icon).clear(holder.icon)
    }

    override fun getItemCount(): Int {
        return deepSearchInfo.size
    }

    @SuppressLint("SetTextI18n")
    private fun Holder.setDeepInfo(searchModel: SearchModel) {
        permissions.text = "${searchModel.permissions} ${context.getString(R.string.permissions)}"
        activities.text = "${searchModel.activities} ${context.getString(R.string.activities)}"
        services.text = "${searchModel.services} ${context.getString(R.string.services)}"
        receivers.text = "${searchModel.receivers} ${context.getString(R.string.receivers)}"
        providers.text = "${searchModel.providers} ${context.getString(R.string.providers)}"
        resources.text = "${searchModel.resources} ${context.getString(R.string.resources)}"
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
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
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
