package app.simple.inure.adapters.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.ViewUtils.gone

class AdapterInformationCustomization(val list: ArrayList<Pair<Int, Int>>) : RecyclerView.Adapter<AdapterInformationCustomization.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_customization, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.gone()
        holder.name.setText(list[position].first)
        holder.checkBox.isChecked = FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), list[position].second)

        holder.checkBox.setOnCheckedChangeListener {
            if (it) {
                if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), list[position].second).invert()) {
                    AppsPreferences.setInfoCustomFilter(FlagUtils.setFlag(AppsPreferences.getInfoCustomFilter(), list[position].second))
                }
            } else {
                if (FlagUtils.isFlagSet(AppsPreferences.getInfoCustomFilter(), list[position].second)) {
                    AppsPreferences.setInfoCustomFilter(FlagUtils.unsetFlag(AppsPreferences.getInfoCustomFilter(), list[position].second))
                }
            }
        }

        holder.container.setOnClickListener {
            holder.checkBox.toggle()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }
}