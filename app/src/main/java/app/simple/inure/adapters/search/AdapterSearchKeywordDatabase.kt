package app.simple.inure.adapters.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class AdapterSearchKeywordDatabase(private val strings: List<String>, private val onClick: (String) -> Unit)
    : RecyclerView.Adapter<AdapterSearchKeywordDatabase.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_search_keyword_database, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = strings[position]

        holder.name.setOnClickListener {
            onClick(strings[position])
        }
    }

    override fun getItemCount(): Int {
        return strings.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: DynamicRippleTextView = itemView.findViewById(R.id.name)
    }
}
