package app.simple.inure.adapters.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.VisibilityCustomizationModel
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.ViewUtils.gone

class AdapterInstallerCustomization(private val list: List<VisibilityCustomizationModel>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_customization, parent, false))
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
            // holder.icon.setImageResource(list[position].icon)
            holder.icon.gone()
            holder.name.setText(list[position].title)
            holder.checkBox.setChecked(InstallerPreferences.getPanelVisibility(list[position].key))

            holder.checkBox.setOnCheckedChangeListener {
                if (it) {
                    InstallerPreferences.setPanelVisibility(list[position].key, true)
                } else {
                    if (isOnlyOnePanelVisible()) {
                        holder.checkBox.setChecked(true)
                    } else {
                        InstallerPreferences.setPanelVisibility(list[position].key, false)
                    }
                }
            }

            holder.container.setOnClickListener {
                if (holder.checkBox.isChecked()) {
                    if (isOnlyOnePanelVisible().invert()) {
                        holder.checkBox.callOnClick()
                    }
                } else {
                    holder.checkBox.callOnClick()
                }
            }
        } else if (holder is Header) {
            holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
            holder.title.setText(R.string.visibility_customization)
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    private fun isOnlyOnePanelVisible(): Boolean {
        var count = 0
        for (i in list) {
            if (InstallerPreferences.getPanelVisibility(i.key)) {
                count++
            }
        }
        return count == 1
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val checkBox: InureCheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}