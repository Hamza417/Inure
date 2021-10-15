package app.simple.inure.adapters.details

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.model.ActivityInfoModel

class AdapterReceivers(private val receivers: MutableList<ActivityInfoModel>, private val packageInfo: PackageInfo)
    : RecyclerView.Adapter<AdapterReceivers.Holder>() {

    private lateinit var receiversCallbacks: ReceiversCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_receivers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.loadIconFromActivityInfo(receivers[position].activityInfo)

        holder.name.text = receivers[position].name.substring(receivers[position].name.lastIndexOf(".") + 1)
        holder.process.text = receivers[position].name

        holder.status.text = holder.itemView.context.getString(
            R.string.activity_status,

            if (receivers[position].exported) {
                holder.itemView.context.getString(R.string.exported)
            } else {
                holder.itemView.context.getString(R.string.not_exported)
            },

            if (ReceiversUtils.isEnabled(holder.itemView.context, packageInfo.packageName, receivers[position].name)) {
                holder.itemView.context.getString(R.string.enabled)
            } else {
                holder.itemView.context.getString(R.string.disabled)
            })

        holder.status.append(receivers[position].status)

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
    }

    override fun getItemCount(): Int {
        return receivers.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_receiver_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_receiver_name)
        val process: TypeFaceTextView = itemView.findViewById(R.id.adapter_receiver_process)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_receiver_status)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_receiver_container)
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