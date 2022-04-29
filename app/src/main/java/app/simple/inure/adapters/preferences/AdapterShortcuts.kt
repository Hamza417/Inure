package app.simple.inure.adapters.preferences

import android.content.ComponentName
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.decorations.checkbox.CheckBox
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.ShortcutModel
import app.simple.inure.util.ConditionUtils.isZero

class AdapterShortcuts(private val list: List<ShortcutModel>, private val shortcuts: MutableList<ShortcutInfoCompat>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_shortcuts, parent, false))
            }
            RecyclerViewConstants.TYPE_HEADER -> {
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
            holder.icon.setImageResource(list[position].icon)
            holder.name.text = list[position].name
            holder.id.text = list[position].id
            holder.action.text = list[position].action

            for (shortcut in shortcuts) {
                if (list[position].id == shortcut.id) {
                    holder.checkBox.setCheckedWithoutAnimations(shortcut.isEnabled)
                    break
                } else {
                    holder.checkBox.setCheckedWithoutAnimations(false)
                }
            }

            holder.checkBox.setOnCheckedChangeListener {
                val intent = Intent(holder.context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.action = list[position].action

                val shortcut = ShortcutInfoCompat.Builder(holder.context, list[position].id)
                    .setShortLabel(list[position].name)
                    .setActivity(ComponentName(holder.context, MainActivity::class.java))
                    .setIcon(IconCompat.createWithResource(holder.context, list[position].icon))
                    .setIntent(intent)
                    .build()

                if (it) {
                    ShortcutManagerCompat.pushDynamicShortcut(holder.context, shortcut)
                } else {
                    ShortcutManagerCompat.removeDynamicShortcuts(holder.context, arrayListOf(shortcut.id))
                }
            }

            holder.container.setOnClickListener {
                holder.checkBox.callOnClick()
            }

        } else if (holder is Header) {
            holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
            holder.title.setText(R.string.shortcuts)
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.shortcut_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.shortcut_name)
        val id: TypeFaceTextView = itemView.findViewById(R.id.shortcut_id)
        val action: TypeFaceTextView = itemView.findViewById(R.id.shortcut_action)
        val checkBox: CheckBox = itemView.findViewById(R.id.shortcut_checkbox)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.shortcut_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}