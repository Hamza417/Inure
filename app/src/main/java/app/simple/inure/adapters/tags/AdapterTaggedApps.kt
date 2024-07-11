package app.simple.inure.adapters.tags

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.FileUtils.toFileOrNull
import app.simple.inure.util.InfoStripUtils.setAppInfo

class AdapterTaggedApps(private val packageInfo: ArrayList<PackageInfo>) : RecyclerView.Adapter<AdapterTaggedApps.Holder>() {

    private var adapterCallbacks: AdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_all_apps_small_details, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = packageInfo[position].packageName
        holder.icon.loadAppIcon(packageInfo[position].packageName,
                                packageInfo[position].applicationInfo.enabled,
                                packageInfo[position].applicationInfo.sourceDir.toFileOrNull())

        holder.name.text = packageInfo[position].applicationInfo.name
        holder.name.setAppVisualStates(packageInfo[position])
        holder.packageName.text = packageInfo[position].packageName
        holder.details.setAppInfo(packageInfo[position])

        holder.container.setOnClickListener {
            adapterCallbacks?.onAppClicked(packageInfo[holder.bindingAdapterPosition], holder.icon)
        }

        holder.container.setOnLongClickListener {
            adapterCallbacks?.onAppLongPressed(packageInfo[holder.bindingAdapterPosition], holder.icon)
            true
        }
    }

    override fun getItemCount(): Int {
        return packageInfo.size
    }

    fun setOnAdapterCallbacks(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    fun getPackageInfo(position: Int): PackageInfo {
        return packageInfo[position]
    }

    fun removeItem(position: Int) {
        packageInfo.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, packageInfo.size)
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val details: TypeFaceTextView = itemView.findViewById(R.id.details)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }
}
