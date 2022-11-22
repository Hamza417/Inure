package app.simple.inure.adapters.menus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.interfaces.menus.BottomMenuCallbacks
import app.simple.inure.util.RecyclerViewUtils
import com.google.android.material.transition.Hold

class AdapterBottomMenu(private val bottomMenuItems: ArrayList<Int>) : RecyclerView.Adapter<HorizontalListViewHolder>() {

    private var bottomMenuCallbacks: BottomMenuCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_menu_divider, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_menu, parent, false))
            }
            else -> {
                throw java.lang.IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: HorizontalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.button.setImageResource(bottomMenuItems[position])

            holder.button.setOnClickListener {
                bottomMenuCallbacks?.onBottomMenuItemClicked(bottomMenuItems[position], it)
            }
        }
    }

    override fun getItemCount(): Int {
        return bottomMenuItems.size
    }

    override fun getItemId(position: Int): Long {
        return bottomMenuItems[position].toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (bottomMenuItems[position] == -1) { // -1 is the divider
            RecyclerViewUtils.TYPE_DIVIDER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    fun setBottomMenuCallbacks(bottomMenuCallbacks: BottomMenuCallbacks) {
        this.bottomMenuCallbacks = bottomMenuCallbacks
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val button: DynamicRippleImageButton = itemView.findViewById(R.id.button)
    }

    inner class Divider(parent: View) : HorizontalListViewHolder(parent)
}