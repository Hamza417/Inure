package app.simple.inure.adapters.menus

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class AdapterTabLayout(private val list: ArrayList<Int>) : RecyclerView.Adapter<AdapterTabLayout.Holder>() {

    private var selected: Int = 0
    private var oldSelected: Int = 0

    private var tabLayoutCallback: TabLayoutCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_tab_layout, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {
        holder.panelName.setText(list[position])

        if (selected == position) {
            holder.panelName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_dot_tiny, 0, 0, 0)
        } else {
            holder.panelName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }

        holder.panelName.setOnClickListener {
            oldSelected = selected
            selected = position
            notifyItemChanged(oldSelected)
            notifyItemChanged(selected)
            tabLayoutCallback?.onTabClicked(position, list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val panelName: DynamicRippleTextView = itemView.findViewById(R.id.panel_name)
    }

    fun layoutPositionChanged(position: Int) {
        oldSelected = selected
        selected = position
        notifyItemChanged(oldSelected)
        notifyItemChanged(selected)
    }

    fun setOnTabLayoutCallbackListener(tabLayoutCallback: TabLayoutCallback) {
        this.tabLayoutCallback = tabLayoutCallback
    }

    companion object {
        interface TabLayoutCallback {
            fun onTabClicked(position: Int, @StringRes res: Int)
        }
    }
}