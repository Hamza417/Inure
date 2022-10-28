package app.simple.inure.adapters.details

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.protectionToString
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.StringUtils.optimizeToColoredString
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AdapterPermissions(private val permissions: MutableList<PermissionInfo>, private val keyword: String)
    : RecyclerView.Adapter<AdapterPermissions.Holder>() {

    private var permissionCallbacks: PermissionCallbacks? = null
    private var permissionLabelMode = PermissionPreferences.getLabelType()
    private val isRootMode = ConfigurationPreferences.isUsingRoot()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_permissions, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (permissions[position].permissionInfo.isNotNull()) {
            holder.name.setPermissionName(position, permissions[position])
            holder.desc.setDescriptionText(holder.itemView.context, permissions[position])
            holder.status.setStatusText(position, holder.itemView.context, permissions[position])

            /* ----------------------------------------------------------------- */

            holder.status.setTextColor(AppearancePreferences.getAccentColor())
            holder.desc.visible(false)
        } else {
            holder.name.text = permissions[position].name.optimizeToColoredString(".")
            holder.status.text = holder.itemView.context.getString(R.string.permission_info_not_available)
            holder.status.setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
            holder.desc.gone()
        }

        if (isRootMode) {
            holder.container.setOnClickListener {
                permissionCallbacks?.onPermissionClicked(it, permissions[position], position)
            }
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
        }
    }

    override fun getItemCount(): Int {
        return permissions.size
    }

    private fun TypeFaceTextView.setStatusText(position: Int, context: Context, permissionInfo: PermissionInfo) {
        @Suppress("deprecation")
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            protectionToString(permissionInfo.permissionInfo!!.protection, permissionInfo.permissionInfo!!.protectionFlags, context)
        } else {
            protectionToString(permissionInfo.permissionInfo!!.protectionLevel, permissionInfo.permissionInfo!!.protectionLevel, context)
        }

        text = when (permissions[position].isGranted) {
            0 -> {
                text.toString() + " | " + context.getString(R.string.rejected)
            }
            1 -> {
                text.toString() + " | " + context.getString(R.string.granted)
            }
            2 -> {
                text.toString() + " | " + context.getString(R.string.unknown)
            }
            else -> {
                text.toString() + " | " + context.getString(R.string.unknown)
            }
        }
    }

    private fun TypeFaceTextView.setDescriptionText(context: Context, permissionInfo: PermissionInfo) {
        text = kotlin.runCatching {
            val string = permissionInfo.permissionInfo!!.loadDescription(context.packageManager)

            if (string.isNullOrEmpty()) {
                throw NullPointerException("Description is either null or not available")
            } else {
                string
            }
        }.getOrElse {
            context.getString(R.string.desc_not_available)
        }
    }

    private fun TypeFaceTextView.setPermissionName(position: Int, permissionInfo: PermissionInfo) {
        text = if (permissionLabelMode) {
            permissionInfo.name
        } else {
            permissions[position].label
        }.toString().optimizeToColoredString(".")
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
    fun permissionStatusChanged(position: Int, grantedStatus: Int) {
        permissions[position].isGranted = grantedStatus
        notifyItemChanged(position)
    }

    fun update() {
        permissionLabelMode = PermissionPreferences.getLabelType()
        for (i in permissions.indices) notifyItemChanged(i)
    }

    fun setOnPermissionCallbacksListener(permissionCallbacks: PermissionCallbacks) {
        this.permissionCallbacks = permissionCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_status)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_desc)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_permissions_container)
    }

    companion object {
        interface PermissionCallbacks {
            fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int)
        }
    }
}
