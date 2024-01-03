package app.simple.inure.adapters.preferences

import android.content.ComponentName
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CheckBox
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils

class AdapterComponentManager(private val list: ArrayList<Triple<Int, Int, Class<out BaseActivity>>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_component_manager, parent, false))
            }
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_typeface, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_.minus(1)

        if (holder is Holder) {
            holder.icon.setImageResource(list[position].first)
            holder.name.setText(list[position].second)

            holder.checkBox.isChecked = when (holder.context.packageManager.getComponentEnabledSetting(ComponentName(holder.context, list[position].third))) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> {
                    ActivityUtils.isEnabled(holder.context, holder.context.packageName, list[position].third.name)
                }
                else -> false
            }

            holder.checkBox.setOnCheckedChangeListener {
                if (it) {
                    holder.context.packageManager.setComponentEnabledSetting(
                            ComponentName(holder.context, list[position].third), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                } else {
                    holder.context.packageManager.setComponentEnabledSetting(
                            ComponentName(holder.context, list[position].third), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                }
            }

            holder.container.setOnClickListener {
                holder.checkBox.callOnClick()
            }

        } else if (holder is Header) {
            holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
            holder.title.setText(R.string.components)
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}