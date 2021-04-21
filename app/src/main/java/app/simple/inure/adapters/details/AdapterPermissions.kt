package app.simple.inure.adapters.details

import android.content.pm.ApplicationInfo
import android.content.pm.PermissionInfo
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

class AdapterPermissions(private val permissions: MutableList<String>, private val applicationInfo: ApplicationInfo) : RecyclerView.Adapter<AdapterPermissions.Holder>() {

    private lateinit var permissionInfo: PermissionInfo
    private val permissionLabelMode = ConfigurationPreferences.getPermissionLabelMode()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_permissions, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        try {
            permissionInfo = permissions[position].getPermissionInfo(holder.itemView.context)!!

            holder.name.text = if (permissionLabelMode) {
                permissionInfo.loadLabel(holder.itemView.context.packageManager)
            } else {
                permissions[position]
            }

            holder.desc.text = try {
                permissionInfo.loadDescription(holder.itemView.context.packageManager)
            } catch (e: NullPointerException) {
                holder.itemView.context.getString(R.string.not_available)
            }

            @Suppress("deprecation")
            holder.status.text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                protectionToString(permissionInfo.protection, holder.itemView.context)
            } else {
                protectionToString(permissionInfo.protectionLevel, holder.itemView.context)
            }
        } catch (e: NullPointerException) {
            holder.name.text = holder.itemView.context.getString(R.string.error)
            holder.status.text = holder.itemView.context.getString(R.string.error)
            holder.desc.text = ""
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