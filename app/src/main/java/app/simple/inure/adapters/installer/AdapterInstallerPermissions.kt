package app.simple.inure.adapters.installer

import android.content.Context
import android.content.pm.PermissionInfo
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.StringUtils.optimizeToColoredString
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AdapterInstallerPermissions(private val permissions: MutableList<String>) : RecyclerView.Adapter<AdapterInstallerPermissions.Holder>() {

    private var permissionLabelMode = PermissionPreferences.getLabelType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_installer_permissions, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        kotlin.runCatching {
            val permissionInfo = with(holder.context.packageManager) {
                getPermissionInfo(permissions[position], 0)
            }
            holder.name.setPermissionName(permissionInfo)
            holder.desc.setDescriptionText(holder.itemView.context, permissionInfo)
            holder.status.setStatusText(permissionInfo)

            /* ----------------------------------------------------------------- */

            holder.status.setTextColor(AppearancePreferences.getAccentColor())
            holder.desc.visible(false)
        }.getOrElse {
            holder.name.text = permissions[position].optimizeToColoredString(".")
            holder.status.text = holder.itemView.context.getString(R.string.permission_info_not_available)
            holder.status.setTextColor(ThemeManager.theme.textViewTheme.secondaryTextColor)
            holder.desc.gone()
        }
    }

    override fun getItemCount(): Int {
        return permissions.size
    }

    private fun TypeFaceTextView.setStatusText(permissionInfo: PermissionInfo) {
        @Suppress("deprecation")
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PermissionUtils.protectionToString(permissionInfo.protection, permissionInfo.protectionFlags, context)
        } else {
            PermissionUtils.protectionToString(permissionInfo.protectionLevel, permissionInfo.protectionLevel, context)
        }
    }

    private fun TypeFaceTextView.setDescriptionText(context: Context, permissionInfo: PermissionInfo) {
        text = kotlin.runCatching {
            val string = permissionInfo.loadDescription(context.packageManager)

            if (string.isNullOrEmpty()) {
                throw NullPointerException("Description is either null or not available")
            } else {
                string
            }
        }.getOrElse {
            context.getString(R.string.desc_not_available)
        }
    }

    private fun TypeFaceTextView.setPermissionName(permissionInfo: PermissionInfo) {
        text = if (permissionLabelMode) {
            permissionInfo.name
        } else {
            permissionInfo.loadLabel(context.packageManager)
        }.toString().optimizeToColoredString(".")
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_status)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_desc)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_permissions_container)
    }
}