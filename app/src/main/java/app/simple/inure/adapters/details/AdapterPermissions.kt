package app.simple.inure.adapters.details

import android.content.pm.ApplicationInfo
import android.content.pm.PermissionInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.PermissionUtils.getPermissionInfo
import app.simple.inure.util.PermissionUtils.protectionToString
import app.simple.inure.util.StringUtils.optimizeToColoredString
import app.simple.inure.util.ViewUtils.makeGoAway

class AdapterPermissions(private val permissions: MutableList<String>, private val applicationInfo: ApplicationInfo)
    : RecyclerView.Adapter<AdapterPermissions.Holder>() {

    private lateinit var permissionInfo: PermissionInfo
    private val permissionLabelMode = ConfigurationPreferences.getPermissionLabelMode()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_permissions, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        runCatching {
            permissionInfo = permissions[position].getPermissionInfo(holder.itemView.context)!!

            holder.name.text = if (permissionLabelMode) {
                permissionInfo.loadLabel(holder.itemView.context.packageManager)
            } else {
                permissions[position]
            }.toString().optimizeToColoredString(holder.itemView.context, ".")

            holder.desc.text = try {
                permissionInfo.loadDescription(holder.itemView.context.packageManager)
            } catch (e: NullPointerException) {
                holder.itemView.context.getString(R.string.not_available)
            }

            holder.status.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                protectionToString(permissionInfo.protection, holder.itemView.context)
            } else {
                @Suppress("deprecation")
                protectionToString(permissionInfo.protectionLevel, holder.itemView.context)
            }
        }.getOrElse {
            holder.name.text = permissions[position].optimizeToColoredString(holder.itemView.context, ".")
            holder.status.text = holder.itemView.context.getString(R.string.desc_not_available)
            holder.desc.makeGoAway()
        }
    }

    override fun getItemCount(): Int {
        return permissions.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_status)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_desc)
    }
}