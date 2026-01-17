package app.simple.inure.adapters.viewers

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ProvidersUtils
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromProviderInfo
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.appendFlag

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

        val context = holder.itemView.context
        val provider = providers[position]

        holder.status.text = buildString {
            appendFlag(
                    if (provider.isExported) {
                        context.getString(R.string.exported)
                } else {
                        context.getString(R.string.not_exported)
                    }
            )

            appendFlag(
                    runCatching {
                        if (ProvidersUtils.isEnabled(context, packageInfo.packageName, provider.name)) {
                            context.getString(R.string.enabled)
                    } else {
                            context.getString(R.string.disabled)
                    }
                }.getOrElse {
                        context.getString(R.string.no_state)
                    }
            )

            appendFlag(provider.status)
        }

        holder.name.setTrackingIcon(provider.trackingId.isNullOrEmpty().not())

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

    class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.adapter_providers_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_package)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_providers_status)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_providers_container)

        init {
            name.enableSelection()
            packageId.enableSelection()
        }
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
