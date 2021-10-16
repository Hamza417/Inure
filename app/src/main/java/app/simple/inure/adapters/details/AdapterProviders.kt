package app.simple.inure.adapters.details

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ProvidersUtils
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.glide.util.ImageLoader.loadIconFromProviderInfo
import app.simple.inure.model.ProviderInfoModel
import app.simple.inure.util.AdapterUtils

class AdapterProviders(private val providers: MutableList<ProviderInfoModel>, private val packageInfo: PackageInfo, val keyword: String)
    : RecyclerView.Adapter<AdapterProviders.Holder>() {

    private lateinit var providersCallbacks: ProvidersCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_providers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.loadIconFromProviderInfo(providers[position].providerInfo)

        holder.name.text = providers[position].name.substring(providers[position].name.lastIndexOf(".") + 1)
        holder.packageId.text = providers[position].name

        holder.status.text = holder.itemView.context.getString(
            R.string.activity_status,

            if (providers[position].isExported) {
                holder.itemView.context.getString(R.string.exported)
            } else {
                holder.itemView.context.getString(R.string.not_exported)
            },

            if (ProvidersUtils.isEnabled(holder.itemView.context, packageInfo.packageName, providers[position].name)) {
                holder.itemView.context.getString(R.string.enabled)
            } else {
                holder.itemView.context.getString(R.string.disabled)
            })

        holder.status.append(providers[position].status)

        holder.authority.text = providers[position].authority

        holder.container.setOnLongClickListener {
            providersCallbacks
                    .onProvidersLongPressed(
                        providers[holder.absoluteAdapterPosition].name,
                        packageInfo,
                        it,
                        ProvidersUtils.isEnabled(holder.itemView.context, packageInfo.packageName, providers[holder.absoluteAdapterPosition].name),
                        holder.absoluteAdapterPosition)
            true
        }

        holder.container.setOnClickListener {
            providersCallbacks
                    .onProvidersClicked(providers[holder.absoluteAdapterPosition])
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
            AdapterUtils.searchHighlighter(holder.packageId, keyword)
        }
    }

    override fun getItemCount(): Int {
        return providers.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_providers_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_package)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_status)
        val authority: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_authority)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_providers_container)
    }

    fun setOnProvidersCallbackListener(providersCallbacks: ProvidersCallbacks) {
        this.providersCallbacks = providersCallbacks
    }

    companion object {
        interface ProvidersCallbacks {
            fun onProvidersClicked(providerInfoModel: ProviderInfoModel)
            fun onProvidersLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int)
        }
    }
}