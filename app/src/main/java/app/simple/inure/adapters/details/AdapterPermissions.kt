package app.simple.inure.adapters.details

import android.content.pm.PermissionInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.PermissionUtils.getPermissionInfo
import app.simple.inure.util.PermissionUtils.protectionToString
import app.simple.inure.util.StringUtils.optimizeToColoredString
import app.simple.inure.util.ViewUtils.makeGoAway
import app.simple.inure.util.ViewUtils.makeVisible

class AdapterPermissions(private val permissions: MutableList<app.simple.inure.model.PermissionInfo>)
    : RecyclerView.Adapter<AdapterPermissions.Holder>() {

    private lateinit var permissionCallbacks: PermissionCallbacks
    private lateinit var permissionInfo: PermissionInfo
    private val permissionLabelMode = ConfigurationPreferences.getPermissionLabelMode()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_permissions, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        runCatching {
            permissionInfo = permissions[position].name.getPermissionInfo(holder.itemView.context)!!

            holder.name.text = if (permissionLabelMode) {
                permissionInfo.loadLabel(holder.itemView.context.packageManager)
            } else {
                permissions[position].name
            }.toString().optimizeToColoredString(holder.itemView.context, ".")

            holder.desc.makeVisible()
            holder.desc.text = try {
                permissionInfo.loadDescription(holder.itemView.context.packageManager)
            } catch (e: NullPointerException) {
                holder.itemView.context.getString(R.string.not_available)
            }

            println(holder.desc.text)

            @Suppress("deprecation")
            holder.status.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                protectionToString(permissionInfo.protectionFlags, holder.itemView.context)
            } else {
                protectionToString(permissionInfo.protectionLevel, holder.itemView.context)
            }

            holder.status.text = if (permissions[position].isGranted) {
                holder.status.text.toString() + " | " + holder.itemView.context.getString(R.string.granted)
            } else {
                holder.status.text.toString() + " | " + holder.itemView.context.getString(R.string.rejected)
            }

            holder.status.setTextColor(holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent))

        }.getOrElse {
            holder.name.text = permissions[position].name.optimizeToColoredString(holder.itemView.context, ".")
            holder.status.text = holder.itemView.context.getString(R.string.desc_not_available)
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textSecondary))
            holder.desc.makeGoAway()
        }

        holder.container.setOnClickListener {
            permissionCallbacks.onPermissionClicked(it, permissions[position])
        }
    }

    override fun getItemCount(): Int {
        return permissions.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_status)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_desc)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_permissions_container)
    }

    fun setOnPermissionCallbacksListener(permissionCallbacks: PermissionCallbacks) {
        this.permissionCallbacks = permissionCallbacks
    }

    companion object {
        interface PermissionCallbacks {
            fun onPermissionClicked(container: View, permissionInfo: app.simple.inure.model.PermissionInfo)
        }
    }
}
