package app.simple.inure.adapters.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.apk.utils.PermissionUtils.getPermissionDescription
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.PermissionUsage
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import java.util.Locale

class AdapterPermissionMonitor(
    private val permissionUsages: ArrayList<PermissionUsage>,
    private val keyword: String = ""
) : RecyclerView.Adapter<AdapterPermissionMonitor.Holder>(), PopupTextProvider {

    private var adapterCallbacks: AdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_permission_monitor, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val usage = permissionUsages[position]

        holder.icon.transitionName = usage.packageInfo.packageName
        holder.icon.loadAppIcon(usage.packageInfo.packageName, usage.packageInfo.safeApplicationInfo.enabled)
        holder.name.text = usage.packageInfo.safeApplicationInfo.name
        holder.packageName.text = usage.packageInfo.packageName
        holder.permission.text = usage.permission.sanitize()
        holder.permissionDescription.text = holder.itemView.context.getPermissionDescription(usage.permissionId)

        // Show time information
        holder.timeInfo.text = buildString {
            if (usage.isActive) {
                append(holder.getString(R.string.active_now))
            } else if (usage.lastAccessTime > 0) {
                append(holder.getString(R.string.last_used))
                append(": ")
                append(DateUtils.getRelativeTimeSpanString(
                    usage.lastAccessTime,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ))
            } else {
                append(holder.getString(R.string.never_used))
            }

            usage.duration?.let {
                append(" | ")
                append(holder.getString(R.string.duration))
                append(": ")
                append(it)
            }
        }

        // Visual indicators
        holder.name.setAppVisualStates(usage.packageInfo)

        // Highlight active permissions
        if (usage.isActive) {
            holder.activeIndicator.visibility = View.VISIBLE
            holder.container.alpha = 1.0f
        } else {
            holder.activeIndicator.visibility = View.GONE
            holder.container.alpha = 0.7f
        }

        // Search highlighting
        AdapterUtils.searchHighlighter(holder.name, keyword)
        AdapterUtils.searchHighlighter(holder.packageName, keyword)
        AdapterUtils.searchHighlighter(holder.permission, keyword)

        // Click listeners
        holder.container.setOnClickListener {
            adapterCallbacks?.onAppClicked(usage.packageInfo, holder.icon)
        }

        holder.container.setOnLongClickListener {
            adapterCallbacks?.onAppLongPressed(usage.packageInfo, holder.icon)
            true
        }
    }

    override fun getItemCount(): Int {
        return permissionUsages.size
    }

    override fun getPopupText(position: Int): String {
        val name = permissionUsages[position].packageInfo.safeApplicationInfo.name
        return if (name.isNullOrEmpty()) {
            ""
        } else {
            name.substring(0, 1).uppercase(Locale.ROOT)
        }
    }

    fun setOnPermissionMonitorCallbackListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    private fun String.sanitize(): String {
        return this.replace("_", " ").lowercase().replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.ROOT)
            } else {
                it.toString()
            }
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_permission_monitor_container)
        val icon: AppIconImageView = itemView.findViewById(R.id.adapter_permission_monitor_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_permission_monitor_name)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.adapter_permission_monitor_package)
        val permission: TypeFaceTextView = itemView.findViewById(R.id.adapter_permission_monitor_permission)
        val permissionDescription: TypeFaceTextView = itemView.findViewById(R.id.adapter_permission_monitor_permission_desc)
        val timeInfo: TypeFaceTextView = itemView.findViewById(R.id.adapter_permission_monitor_time)
        val activeIndicator: View = itemView.findViewById(R.id.adapter_permission_monitor_active_indicator)
    }
}
