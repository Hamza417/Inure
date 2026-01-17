package app.simple.inure.adapters.viewers

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ServicesUtils
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromServiceInfo
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.appendFlag
import com.bumptech.glide.Glide

class AdapterServices(private val services: MutableList<ServiceInfoModel>, private val packageInfo: PackageInfo, private val keyword: String) : RecyclerView.Adapter<AdapterServices.Holder>() {

    private lateinit var servicesCallbacks: ServicesCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_services, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.loadIconFromServiceInfo(services[position].serviceInfo)
        holder.name.text = services[position].name.substring(services[position].name.lastIndexOf(".") + 1)
        holder.packageId.text = services[position].name
        holder.name.setTrackingIcon(services[position].trackerId.isNullOrEmpty().not())

        val context = holder.itemView.context
        val service = services[position]

        holder.status.text = buildString {
            appendFlag(
                    if (service.isExported) {
                        context.getString(R.string.exported)
                    } else {
                        context.getString(R.string.not_exported)
                    }
            )

            appendFlag(
                    runCatching {
                        if (ServicesUtils.isEnabled(context, packageInfo.packageName, service.name)) {
                            context.getString(R.string.enabled)
                        } else {
                            context.getString(R.string.disabled)
                        }
                    }.getOrElse {
                        context.getString(R.string.no_state)
                    }
            )

            appendFlag(service.status)
        }

        holder.container.setOnLongClickListener {
            servicesCallbacks
                .onServiceLongPressed(
                        services[holder.absoluteAdapterPosition].name,
                        packageInfo,
                        it,
                        ServicesUtils.isEnabled(holder.itemView.context, packageInfo.packageName, services[holder.absoluteAdapterPosition].name),
                        holder.absoluteAdapterPosition)
            true
        }

        holder.container.setOnClickListener {
            servicesCallbacks
                .onServiceClicked(services[position])
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
            AdapterUtils.searchHighlighter(holder.packageId, keyword)
        }
    }

    override fun getItemCount(): Int {
        return services.size
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        Glide.with(holder.icon.context).clear(holder.icon)
    }

    class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.adapter_services_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_package)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_status)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_services_container)

        init {
            name.enableSelection()
            packageId.enableSelection()
        }
    }

    fun setOnServiceCallbackListener(servicesCallbacks: ServicesCallbacks) {
        this.servicesCallbacks = servicesCallbacks
    }

    companion object {
        interface ServicesCallbacks {
            fun onServiceClicked(serviceInfoModel: ServiceInfoModel)
            fun onServiceLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int)
        }
    }
}
