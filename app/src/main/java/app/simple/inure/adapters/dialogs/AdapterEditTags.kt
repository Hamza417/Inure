package app.simple.inure.adapters.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.views.TagChip
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.viewmodels.dialogs.EditTagViewModel.Companion.Package

class AdapterEditTags(private val packages: MutableList<Package>)
    : RecyclerView.Adapter<AdapterEditTags.Holder>() {

    private var callback: EditTagsCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_tags_add, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.tag.text = packages[position].appName
        holder.tag.setChipIconResource(R.drawable.ic_close_12dp)
        holder.tag.chipStartPadding = 25F

        if (AccessibilityPreferences.isColorfulIcons()) {
            try {
                holder.tag.setChipColor(Colors.getColors()[position], true)
            } catch (_: IndexOutOfBoundsException) {
                holder.tag.setChipColor(Colors.getColors()[position - Colors.getColors().size], true)
            }
        } else {
            holder.tag.setDefaultChipColor()
        }

        holder.tag.setOnClickListener {
            callback?.onTagClicked(packages[position])
        }
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val tag: TagChip = itemView.findViewById(R.id.tag_chip)
    }

    fun removePackage(tag: Package) {
        val index = packages.indexOf(tag)
        packages.remove(tag)
        notifyItemRemoved(index)
        notifyItemRangeChanged(0, packages.size)
    }

    fun setOnCallbackListener(callback: EditTagsCallback) {
        this.callback = callback
    }

    companion object {
        interface EditTagsCallback {
            fun onTagClicked(pkg: Package)
        }
    }
}
