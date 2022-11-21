package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
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
import app.simple.inure.models.SearchModel
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.PackageListUtils.setAppInfo

class AdapterDeepSearch(private var deepSearchInfo: ArrayList<SearchModel>, private var searchKeyword: String = "") : RecyclerView.Adapter<AdapterDeepSearch.Holder>() {

    private lateinit var adapterCallbacks: AdapterCallbacks
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
        holder.deepInfo.setDeepInfo(deepSearchInfo[position])

        holder.container.setOnClickListener {
            adapterCallbacks.onAppClicked(deepSearchInfo[position].packageInfo, holder.icon)
        }

        if (searchKeyword.isNotEmpty()) {
            AdapterUtils.searchHighlighter(holder.name, searchKeyword, ignoreCasing)
            AdapterUtils.searchHighlighter(holder.packageId, searchKeyword, ignoreCasing)
            AdapterUtils.searchHighlighter(holder.deepInfo, searchKeyword, ignoreCasing)
        }

        holder.container.setOnLongClickListener {
            adapterCallbacks.onAppLongPressed(deepSearchInfo[position].packageInfo, holder.icon)
            true
        }
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        GlideApp.with(holder.icon).clear(holder.icon)
    }

    override fun getItemCount(): Int {
        return deepSearchInfo.size
    }

    private fun TypeFaceTextView.setDeepInfo(searchModel: SearchModel) {
        val stringBuilder = StringBuilder()

        stringBuilder.append("${searchModel.permissions} ${context.getString(R.string.permissions)}")
        stringBuilder.append(" • ")
        stringBuilder.append("${searchModel.activities} ${context.getString(R.string.activities)}")
        stringBuilder.append(" • ")
        stringBuilder.append("${searchModel.services} ${context.getString(R.string.services)}")
        stringBuilder.append(" • ")
        stringBuilder.append("${searchModel.receivers} ${context.getString(R.string.receivers)}")
        stringBuilder.append(" • ")
        stringBuilder.append("${searchModel.providers} ${context.getString(R.string.providers)}")
        stringBuilder.append(" • ")
        stringBuilder.append("${searchModel.resources} ${context.getString(R.string.resources)}")

        text = stringBuilder
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_all_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_all_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.adapter_all_app_info)
        val deepInfo: TypeFaceTextView = itemView.findViewById(R.id.adapter_deep_info)
        val container: ConstraintLayout = itemView.findViewById(R.id.adapter_all_app_container)
    }
}
