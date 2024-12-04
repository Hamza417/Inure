package app.simple.inure.adapters.viewers

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.decorations.condensed.CondensedConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.util.AdapterUtils

class AdapterReceivers(private val receivers: MutableList<ActivityInfoModel>, private val packageInfo: PackageInfo, val keyword: String)
    : RecyclerView.Adapter<AdapterReceivers.Holder>() {

    private lateinit var receiversCallbacks: ReceiversCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_receivers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.loadIconFromActivityInfo(receivers[position].activityInfo)
        holder.name.text = receivers[position].name.substring(receivers[position].name.lastIndexOf(".") + 1)
        holder.packageId.text = receivers[position].name
        holder.status.text = holder.itemView.context.getString(
                R.string.activity_status,

                if (receivers[position].exported) {
                    holder.itemView.context.getString(R.string.exported)
                } else {
                    holder.itemView.context.getString(R.string.not_exported)
                },

                kotlin.runCatching {
                    if (ReceiversUtils.isEnabled(holder.itemView.context, packageInfo.packageName, receivers[position].name)) {
                        holder.itemView.context.getString(R.string.enabled)
                    } else {
                        holder.itemView.context.getString(R.string.disabled)
                    }
                }.getOrElse {
                    holder.itemView.context.getString(R.string.no_state)
                })
        holder.status.append(receivers[position].status)
        holder.name.setTrackingIcon(receivers[position].trackerId.isNullOrEmpty().not())

        holder.container.setOnLongClickListener {
            receiversCallbacks
                .onReceiverLongPressed(
                        receivers[holder.absoluteAdapterPosition].name,
                        packageInfo,
                        it,
                        ReceiversUtils.isEnabled(holder.itemView.context, packageInfo.packageName, receivers[holder.absoluteAdapterPosition].name),
                        holder.absoluteAdapterPosition)
            true
        }

        holder.container.setOnClickListener {
            receiversCallbacks
                .onReceiverClicked(receivers[holder.absoluteAdapterPosition])
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
            AdapterUtils.searchHighlighter(holder.packageId, keyword)
        }
    }

    override fun getItemCount(): Int {
        return receivers.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.process)
        val status: TypeFaceTextView = itemView.findViewById(R.id.status)
        val container: CondensedConstraintLayout = itemView.findViewById(R.id.container)

        init {
            name.enableSelection()
            packageId.enableSelection()
        }
    }

    fun setOnReceiversCallbackListener(receiversCallbacks: ReceiversCallbacks) {
        this.receiversCallbacks = receiversCallbacks
    }

    companion object {
        interface ReceiversCallbacks {
            fun onReceiverClicked(activityInfoModel: ActivityInfoModel)
            fun onReceiverLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int)
        }
    }
}
