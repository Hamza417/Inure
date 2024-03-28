package app.simple.inure.adapters.batch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.menus.BatchActionsCallback
import app.simple.inure.util.RecyclerViewUtils

class AdapterBatchActions(private val bottomMenuItems: ArrayList<Pair<Int, Int>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var batchActionsCallback: BatchActionsCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_divider_batch_actions, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_batch_actions, parent, false))
            }
            else -> {
                throw java.lang.IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.button.setImageResource(bottomMenuItems[position].first)
            holder.button.contentDescription = holder.itemView.context.getString(bottomMenuItems[position].second)
            holder.text.text = holder.itemView.context.getString(bottomMenuItems[position].second)

            holder.container.setOnClickListener {
                batchActionsCallback?.onBatchMenuItemClicked(bottomMenuItems[position].first, it)
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

    fun setBatchActionsCallbackListener(batchActionsCallback: BatchActionsCallback) {
        this.batchActionsCallback = batchActionsCallback
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val button: ThemeIcon = itemView.findViewById(R.id.button)
        val text: TypeFaceTextView = itemView.findViewById(R.id.text)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.container)
    }

    inner class Divider(parent: View) : VerticalListViewHolder(parent)
}
