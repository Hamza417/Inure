package app.simple.inure.adapters.preferences

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.adapters.PreferencesCallbacks
import app.simple.inure.models.PreferenceSearchModel
import app.simple.inure.util.AdapterUtils

class AdapterPreferenceSearch : RecyclerView.Adapter<AdapterPreferenceSearch.Holder>() {

    private var preferencesCallbacks: PreferencesCallbacks? = null

    var keyword: String? = null
    var list = arrayListOf<PreferenceSearchModel>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_preferences_search, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        if (list[position].icon > 0) {
            holder.icon.setImageResource(list[position].icon)
        }

        holder.title.setText(list[position].title)
        holder.description.setText(list[position].description)

        with(StringBuilder()) {
            append(holder.getString(list[position].panel))
            append(" -> ")
            append(holder.getString(list[position].category))
            append(" -> ")
            append(holder.getString(list[position].title))

            holder.path.text = this
        }

        with(StringBuilder()) {
            append(holder.getString(list[position].panel))
            append(" | ")
            append(holder.getString(list[position].type))

            holder.flags.text = this
        }

        holder.container.setOnClickListener {
            preferencesCallbacks?.onPrefsClicked(holder.icon, list[position].panel, position)
        }

        if (!keyword.isNullOrEmpty()) {
            AdapterUtils.searchHighlighter(holder.title, keyword!!)
            AdapterUtils.searchHighlighter(holder.description, keyword!!)
            AdapterUtils.searchHighlighter(holder.path, keyword!!)
            AdapterUtils.searchHighlighter(holder.flags, keyword!!)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.icon);
        val title: TypeFaceTextView = itemView.findViewById(R.id.title)
        val description: TypeFaceTextView = itemView.findViewById(R.id.description)
        val flags: TypeFaceTextView = itemView.findViewById(R.id.flags)
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_preference_search_container)
    }

    fun setOnPreferencesCallbackListener(preferencesCallbacks: PreferencesCallbacks) {
        this.preferencesCallbacks = preferencesCallbacks
    }
}