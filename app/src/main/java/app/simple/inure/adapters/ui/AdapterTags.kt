package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.tags.AdapterTaggedIcons
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleMaterialCardView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.Tag
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.RecyclerViewUtils

class AdapterTags(val tags: ArrayList<Tag>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_tags, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_tags, parent, false))
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        when (holder) {
            is Header -> {
                holder.total.text = holder.itemView.context.getString(R.string.total_tags, tags.size)
            }
            is Holder -> {
                holder.tag.text = tags[position - 1].tag
                holder.recyclerView.adapter = AdapterTaggedIcons(tags[position - 1].packages.split(","))
                holder.date.text = tags[position - 1].dateAdded.toDate()

                holder.container.setOnClickListener {

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return tags.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val tag: TypeFaceTextView = itemView.findViewById(R.id.tag)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view)
        val date: TypeFaceTextView = itemView.findViewById(R.id.date_updated)
        val container: DynamicRippleMaterialCardView = itemView.findViewById(R.id.container)

        init {
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(itemView.context, 4)
        }
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.total_tags)

        init {
            val params = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }
}