package app.simple.inure.adapters.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterPreferences(private val list: ArrayList<Pair<Int, Int>>) : RecyclerView.Adapter<AdapterPreferences.Holder>() {

    private var preferencesCallbacks: PreferencesCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_preferences, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.text.text = holder.itemView.context.getString(list[position].second)
        holder.icon.setImageResource(list[position].first)
        holder.icon.transitionName = holder.itemView.context.getString(list[position].second)

        holder.container.setOnClickListener {
            preferencesCallbacks?.onPrefsClicked(holder.icon, list[position].second)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val text: TypeFaceTextView = itemView.findViewById(R.id.preferences_text)
        val icon: ImageView = itemView.findViewById(R.id.preferences_icon)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.pref_container)
    }

    fun setOnPreferencesCallbackListener(preferencesCallbacks: PreferencesCallbacks) {
        this.preferencesCallbacks = preferencesCallbacks
    }

    companion object {
        interface PreferencesCallbacks {
            fun onPrefsClicked(imageView: ImageView, category: Int)
        }
    }
}