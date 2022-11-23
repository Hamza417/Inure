package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.StackTrace
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.RecyclerViewUtils

class AdapterStackTraces(val stackTraces: ArrayList<StackTrace>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterCallbacks: AdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_stacktraces, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_stacktraces, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            holder.message.text = stackTraces[position].message
            holder.cause.text = stackTraces[position].cause
            holder.trace.text = stackTraces[position].trace
            holder.timestamp.text = stackTraces[position].timestamp.toDate()

            holder.container.setOnClickListener {
                adapterCallbacks?.onStackTraceClicked(stackTraces[position])
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onStackTraceLongClicked(
                        stackTraces[holder.bindingAdapterPosition.minus(1)],
                        it, holder.bindingAdapterPosition.minus(1))
                true
            }
        } else if (holder is Header) {
            holder.total.text = stackTraces.size.toString()
        }
    }

    override fun getItemCount(): Int {
        return stackTraces.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    fun itemRemoved(position: Int) {
        stackTraces.removeAt(position)
        notifyItemChanged(0)
        notifyItemRemoved(position.plus(1))
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val message: TypeFaceTextView = itemView.findViewById(R.id.message)
        val cause: TypeFaceTextView = itemView.findViewById(R.id.cause)
        val timestamp: TypeFaceTextView = itemView.findViewById(R.id.timestamp)
        val trace: TypeFaceTextView = itemView.findViewById(R.id.trace)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.total)
    }
}