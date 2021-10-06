package app.simple.inure.adapters.details

import android.content.Context
import android.content.pm.PermissionInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionInfo
import app.simple.inure.apk.utils.PermissionUtils.protectionToString
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.StringUtils.optimizeToColoredString
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AdapterPermissions(private val permissions: MutableList<app.simple.inure.model.PermissionInfo>)
    : RecyclerView.Adapter<AdapterPermissions.Holder>() {

    private lateinit var permissionCallbacks: PermissionCallbacks
    private var permissionInfo: PermissionInfo? = null
    private val permissionLabelMode = ConfigurationPreferences.getPermissionLabelMode()
    private val isRootMode = ConfigurationPreferences.isUsingRoot()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_permissions, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        runCatching {
            permissionInfo = permissions[position].name.getPermissionInfo(holder.itemView.context)!!

            /* ----------------------------------------------------------------- */

            holder.name.setPermissionName(position, holder.itemView.context)
            holder.desc.setDescriptionText(holder.itemView.context)
            holder.status.setStatusText(position, holder.itemView.context)

            /* ----------------------------------------------------------------- */

            holder.status.setTextColor(holder.itemView.context.resolveAttrColor(R.attr.colorAppAccent))
            holder.desc.visible()

        }.getOrElse {
            holder.name.text = permissions[position].name.optimizeToColoredString(holder.itemView.context, ".")
            holder.status.text = holder.itemView.context.getString(R.string.permission_info_not_available)
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textSecondary))
            holder.desc.gone()
        }

        if (isRootMode) {
            holder.container.setOnClickListener {
                permissionCallbacks.onPermissionClicked(it, permissions[position], position)
            }
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

    private fun TypeFaceTextView.setStatusText(position: Int, context: Context) {
        @Suppress("deprecation")
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            protectionToString(permissionInfo!!.protection, permissionInfo!!.protectionFlags, context)
        } else {
            protectionToString(permissionInfo!!.protectionLevel, permissionInfo!!.protectionLevel, context)
        }

        text = if (permissions[position].isGranted) {
            text.toString() + " | " + context.getString(R.string.granted)
        } else {
            text.toString() + " | " + context.getString(R.string.rejected)
        }
    }

    private fun TypeFaceTextView.setDescriptionText(context: Context) {
        text = kotlin.runCatching {
            val string = permissionInfo!!.loadDescription(context.packageManager)

            if (string.isNullOrEmpty()) {
                throw NullPointerException("Description is either null or not available")
            } else {
                string
            }
        }.getOrElse {
            context.getString(R.string.desc_not_available)
        }
    }

    private fun TypeFaceTextView.setPermissionName(position: Int, context: Context) {
        text = if (permissionLabelMode) {
            permissionInfo!!.loadLabel(context.packageManager)
        } else {
            permissions[position].name
        }.toString().optimizeToColoredString(context, ".")
    }

    /**
     * Called or should be called to update when any of the permission's
     * status is changed such as in situation when permission status is
     * revoked or granted.
     *
     * @param position item's position in the list whose status
     *                 is being changed
     * @param grantedStatus status of the permission if it is granted or not
     */
    fun permissionStatusChanged(position: Int, grantedStatus: Boolean) {
        permissions[position].isGranted = grantedStatus
        notifyItemChanged(position)
    }

    fun setOnPermissionCallbacksListener(permissionCallbacks: PermissionCallbacks) {
        this.permissionCallbacks = permissionCallbacks
    }

    companion object {
        interface PermissionCallbacks {
            fun onPermissionClicked(container: View, permissionInfo: app.simple.inure.model.PermissionInfo, position: Int)
        }
    }
}
