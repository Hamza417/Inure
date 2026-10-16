package app.simple.inure.adapters.tags

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.FileUtils.toFileOrNull
import app.simple.inure.util.InfoStripUtils.setAppInfo
import app.simple.inure.util.RecyclerViewUtils

class AdapterUntaggedApps(private val packageInfo: ArrayList<PackageInfo>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterCallbacks: AdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_untagged_apps, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_all_apps_small_details, parent, false))
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), packageInfo.size)
        } else if (holder is Holder) {
            val itemPosition = holder.bindingAdapterPosition - 1
            holder.icon.transitionName = packageInfo[itemPosition].packageName
            holder.icon.loadAppIcon(packageInfo[itemPosition].packageName,
                                    packageInfo[itemPosition].safeApplicationInfo.enabled,
                                    packageInfo[itemPosition].safeApplicationInfo.sourceDir.toFileOrNull())

            holder.name.text = packageInfo[itemPosition].safeApplicationInfo.name
            holder.name.setAppVisualStates(packageInfo[itemPosition])
            holder.packageName.text = packageInfo[itemPosition].packageName
            holder.details.setAppInfo(packageInfo[itemPosition])

            holder.container.setOnClickListener {
                adapterCallbacks?.onAppClicked(packageInfo[itemPosition], holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onAppLongPressed(packageInfo[itemPosition], holder.icon)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return packageInfo.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    fun setOnAdapterCallbacks(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    fun getPackageInfo(position: Int): PackageInfo {
        return packageInfo[position - 1]
    }

    fun removeItem(position: Int) {
        packageInfo.removeAt(position - 1)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, packageInfo.size + 1)
        notifyItemChanged(0) // Update header
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val details: TypeFaceTextView = itemView.findViewById(R.id.details)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.total_apps)
    }
}
