package app.simple.inure.adapters.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.adapters.PreferencesCallbacks
import app.simple.inure.util.ConditionUtils.isZero

class AdapterPreferences(private val list: ArrayList<Pair<Int, Int>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var preferencesCallbacks: PreferencesCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_preferences, parent, false))
            }
            RecyclerViewConstants.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_divider_preferences, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.text.text = holder.itemView.context.getString(list[position].second)
            holder.icon.setImageResource(list[position].first)
            holder.icon.transitionName = holder.itemView.context.getString(list[position].second)

            holder.container.setOnClickListener {
                preferencesCallbacks?.onPrefsClicked(holder.icon, list[position].second, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].first.isZero()) {
            RecyclerViewConstants.TYPE_DIVIDER
        } else RecyclerViewConstants.TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val text: TypeFaceTextView = itemView.findViewById(R.id.preferences_text)
        val icon: ImageView = itemView.findViewById(R.id.preferences_icon)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.pref_container)
    }

    inner class Divider(itemView: View) : VerticalListViewHolder(itemView)

    fun setOnPreferencesCallbackListener(preferencesCallbacks: PreferencesCallbacks) {
        this.preferencesCallbacks = preferencesCallbacks
    }
}