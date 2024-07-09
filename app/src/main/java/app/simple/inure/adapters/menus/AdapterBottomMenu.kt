package app.simple.inure.adapters.menus

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.menus.BottomMenuCallbacks
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.RecyclerViewUtils

class AdapterBottomMenu(private val bottomMenuItems: ArrayList<Pair<Int, Int>>) : RecyclerView.Adapter<HorizontalListViewHolder>() {

    private var bottomMenuCallbacks: BottomMenuCallbacks? = null
    private val isBottomMenuContext = AccessibilityPreferences.isAppElementsContext()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_menu_divider, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                if (isBottomMenuContext) {
                    HolderContext(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_menu_context, parent, false))
                } else {
                    Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bottom_menu, parent, false))
                }
            }
            else -> {
                throw java.lang.IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: HorizontalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.button.setImageResource(bottomMenuItems[position].first)
            holder.button.contentDescription = holder.itemView.context.getString(bottomMenuItems[position].second)

            holder.button.setOnClickListener {
                bottomMenuCallbacks?.onBottomMenuItemClicked(bottomMenuItems[position].first, it)
            }

            // We don't need else condition here since it's going to be same for all elements
            if (AppearancePreferences.isAccentColorOnBottomMenu()) {
                holder.button.setRippleColor(RIPPLE_COLOR)
            }

            when {
                AppearancePreferences.isAccentColorOnBottomMenu() -> {
                    holder.button.imageTintList = ColorStateList.valueOf(Color.WHITE)
                }
                AccessibilityPreferences.isColorfulIcons() -> {
                    holder.button.imageTintList = ColorStateList(arrayOf(intArrayOf(
                            android.R.attr.state_enabled
                    ), intArrayOf()), intArrayOf(
                            Colors.getColors()[position],
                            Colors.getColors()[position]
                    ))
                }
            }
        } else if (holder is HolderContext) {
            holder.button.setImageResource(bottomMenuItems[position].first)
            holder.button.contentDescription = holder.itemView.context.getString(bottomMenuItems[position].second)
            holder.text.text = holder.itemView.context.getString(bottomMenuItems[position].second)

            holder.container.setOnClickListener {
                bottomMenuCallbacks?.onBottomMenuItemClicked(bottomMenuItems[position].first, it)
            }

            // // We don't need else condition here since it's going to be same for all elements
            if (AppearancePreferences.isAccentColorOnBottomMenu()) {
                holder.container.setRippleColor(RIPPLE_COLOR)
            }

            when {
                AppearancePreferences.isAccentColorOnBottomMenu() -> {
                    holder.button.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    holder.text.setTextColor(Color.WHITE)
                }
                AccessibilityPreferences.isColorfulIcons() -> {
                    holder.button.imageTintList = ColorStateList(arrayOf(intArrayOf(
                            android.R.attr.state_enabled
                    ), intArrayOf()), intArrayOf(
                            Colors.getColors()[position],
                            Colors.getColors()[position]
                    ))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return bottomMenuItems.size
    }

    override fun getItemId(position: Int): Long {
        return bottomMenuItems[position].first.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (bottomMenuItems[position].first == -1) { // -1 is the divider
            RecyclerViewUtils.TYPE_DIVIDER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    fun setBottomMenuCallbacks(bottomMenuCallbacks: BottomMenuCallbacks) {
        this.bottomMenuCallbacks = bottomMenuCallbacks
    }

    fun getBottomMenuItems(): ArrayList<Pair<Int, Int>> {
        return bottomMenuItems
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMenu(bottomMenuItems: ArrayList<Pair<Int, Int>>) {
        Log.d("AdapterBottomMenu", "updateMenu: $bottomMenuItems")
        this.bottomMenuItems.clear()
        this.bottomMenuItems.addAll(bottomMenuItems)
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val button: DynamicRippleImageButton = itemView.findViewById(R.id.button)
    }

    inner class HolderContext(itemView: View) : HorizontalListViewHolder(itemView) {
        val button: ThemeIcon = itemView.findViewById(R.id.button)
        val text: TypeFaceTextView = itemView.findViewById(R.id.text)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.container)
    }

    inner class Divider(parent: View) : HorizontalListViewHolder(parent) {
        val divider: View = parent.findViewById(R.id.divider)

        init {
            val layoutParams = divider.layoutParams
            layoutParams.height = MainPreferences.getBottomMenuHeight()
            divider.layoutParams = layoutParams
        }
    }

    companion object {
        private const val RIPPLE_COLOR = 0x80FFFFFF.toInt()
    }
}
