package app.simple.inure.adapters.viewers

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.ViewUtils.visible

class AdapterBoot(private val resolveInfoList: ArrayList<ResolveInfo>, val keyword: String)
    : RecyclerView.Adapter<AdapterBoot.Holder>() {

    private lateinit var bootCallbacks: BootCallbacks
    private val isRoot = ConfigurationPreferences.isUsingRoot()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_boot, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val isEnabled = ReceiversUtils.isEnabled(
                holder.itemView.context, resolveInfoList[position].activityInfo.packageName, resolveInfoList[position].activityInfo.name)

        holder.icon.loadIconFromActivityInfo(resolveInfoList[position].activityInfo)
        holder.name.text = resolveInfoList[position].activityInfo.name.substring(resolveInfoList[position].activityInfo.name.lastIndexOf(".") + 1)
        holder.packageId.text = resolveInfoList[position].activityInfo.name
        holder.status.text = holder.itemView.context.getString(
                R.string.activity_status,

                if (resolveInfoList[position].activityInfo.exported) {
                    holder.itemView.context.getString(R.string.exported)
                } else {
                    holder.itemView.context.getString(R.string.not_exported)
                },

                if (isEnabled) {
                    holder.itemView.context.getString(R.string.enabled)
                } else {
                    holder.itemView.context.getString(R.string.disabled)
                }
        )
        holder.switch.isChecked = isEnabled
        // holder.status.append(receivers[position].status)
        // holder.name.setTrackingIcon(receivers[position].trackerId.isNullOrEmpty().not())

        if (isRoot) {
            holder.switch.visible(false)

            holder.switch.setOnSwitchCheckedChangeListener {
                bootCallbacks.onBootSwitchChanged(resolveInfoList[position], it)
            }

            holder.container.setOnLongClickListener {
                bootCallbacks
                    .onBootLongPressed(
                            resolveInfoList[holder.absoluteAdapterPosition].activityInfo.name,
                            it,
                            ReceiversUtils.isEnabled(holder.itemView.context, resolveInfoList[position].activityInfo.packageName, resolveInfoList[holder.absoluteAdapterPosition].activityInfo.name),
                            holder.absoluteAdapterPosition)
                true
            }

            holder.container.setOnClickListener {
                bootCallbacks
                    .onBootClicked(resolveInfoList[holder.absoluteAdapterPosition], holder.switch.isChecked)
            }
        } else {
            holder.switch.gone()
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
            AdapterUtils.searchHighlighter(holder.packageId, keyword)
        }
    }

    override fun getItemCount(): Int {
        return resolveInfoList.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.id)
        val status: TypeFaceTextView = itemView.findViewById(R.id.status)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
        val switch: Switch = itemView.findViewById(R.id.switch_view)

        init {
            name.enableSelection()
            packageId.enableSelection()
        }
    }

    fun setBootCallbacks(bootCallbacks: BootCallbacks) {
        this.bootCallbacks = bootCallbacks
    }

    fun updateBoot(it: ResolveInfo?) {
        val index = resolveInfoList.indexOf(it)
        notifyItemChanged(index)
    }

    companion object {
        interface BootCallbacks {
            fun onBootClicked(resolveInfo: ResolveInfo, checked: Boolean)
            fun onBootLongPressed(packageId: String, icon: View, isComponentEnabled: Boolean, position: Int)
            fun onBootSwitchChanged(resolveInfo: ResolveInfo, checked: Boolean)
        }
    }
}
