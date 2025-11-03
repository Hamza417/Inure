package app.simple.inure.adapters.installer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.views.TagChip
import app.simple.inure.themes.manager.ThemeManager

class AdapterTabBar(private val titles: ArrayList<String>) : RecyclerView.Adapter<AdapterTabBar.Holder>() {

    private var callback: TabBarCallback? = null

    var selectedPosition = 0
        set(value) {
            notifyItemChanged(field)
            field = value
            notifyItemChanged(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_tags_add, parent, false))
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.tag.text = titles[position]

        holder.tag.setOnClickListener {
            callback?.onTabClicked(position)
            selectedPosition = position
        }

        if (position == selectedPosition) {
            holder.tag.setDefaultChipColor()
        } else {
            holder.tag.setChipColor(ThemeManager.theme.viewGroupTheme.background, false)
        }
    }

    class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val tag: TagChip = itemView.findViewById(R.id.tag_chip)
    }

    fun setOnTabBarClickListener(callback: TabBarCallback) {
        this.callback = callback
    }

    companion object {
        interface TabBarCallback {
            fun onTabClicked(position: Int)
        }
    }
}