package app.simple.inure.adapters.details

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ProvidersUtils
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import com.jaredrummler.apkparser.model.AndroidComponent

class AdapterProviders(private val providers: List<AndroidComponent>, private val packageInfo: PackageInfo)
    : RecyclerView.Adapter<AdapterProviders.Holder>() {

    private lateinit var providersCallbacks: ProvidersCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_services, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = providers[position].name.substring(providers[position].name.lastIndexOf(".") + 1)
        holder.process.text = providers[position].name

        holder.status.text = holder.itemView.context.getString(
            R.string.activity_status,

            if (providers[position].exported) {
                holder.itemView.context.getString(R.string.exported)
            } else {
                holder.itemView.context.getString(R.string.not_exported)
            },

            if (ProvidersUtils.isEnabled(holder.itemView.context, packageInfo.packageName, providers[position].name)) {
                holder.itemView.context.getString(R.string.enabled)
            } else {
                holder.itemView.context.getString(R.string.disabled)
            })

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
    }

    override fun getItemCount(): Int {
        return providers.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_name)
        val process: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_process)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_service_status)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_services_container)
    }

    fun setOnProvidersCallbackListener(providersCallbacks: ProvidersCallbacks) {
        this.providersCallbacks = providersCallbacks
    }

    companion object {
        interface ProvidersCallbacks {
            fun onProvidersLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int)
        }
    }
}