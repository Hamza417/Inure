package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.views.TagChip

class AdapterTags(private val tags: ArrayList<String>) : RecyclerView.Adapter<AdapterTags.Holder>() {

    private var callback: TagsCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return when (viewType) {
            TYPE_TAG -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_tags_add, parent, false))
            }
            TYPE_ADD -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_tags_add, parent, false))
            }
            else -> {
                throw IllegalStateException("Unexpected value: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (tags.size == 0 || position == tags.size) {
            holder.tag.text = holder.itemView.context.getString(R.string.add_tag)
            holder.tag.setChipIconResource(R.drawable.ic_add)
            holder.tag.chipStartPadding = 25F

            holder.tag.setOnClickListener {
                callback?.onAddClicked()
            }
        } else {
            holder.tag.text = tags[position]
            holder.tag.isChipIconVisible = false

            holder.tag.setOnClickListener {
                callback?.onTagClicked(tags[position])
            }
        }

        try {
            holder.tag.setChipColor(Colors.getColors()[position], true)
        } catch (e: IndexOutOfBoundsException) {
            holder.tag.setChipColor(Colors.getColors()[position - Colors.getColors().size], true)
        }
    }

    override fun getItemCount(): Int {
        return if (tags.size == 0) {
            1
        } else {
            tags.size.plus(1)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (tags.size == 0) {
            TYPE_ADD
        } else {
            if (position == tags.size) {
                TYPE_ADD
            } else {
                TYPE_TAG
            }
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tag: TagChip = itemView.findViewById(R.id.tag_chip)
    }

    fun addTag(tag: String) {
        tags.add(tag)
        notifyItemInserted(tags.size)
    }

    fun removeTag(tag: String) {
        val index = tags.indexOf(tag)
        tags.remove(tag)
        notifyItemRemoved(index)
    }

    fun setOnTagCallbackListener(callback: TagsCallback) {
        this.callback = callback
    }

    companion object {
        private const val TAG = "AdapterTags"
        private const val TYPE_TAG = 0
        private const val TYPE_ADD = 1

        interface TagsCallback {
            fun onTagClicked(tag: String)
            fun onAddClicked()
        }
    }
}