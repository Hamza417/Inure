package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.models.StackTrace
import app.simple.inure.util.DateUtils.toDate

class AdapterStackTraces(val stackTraces: ArrayList<StackTrace>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var appsAdapterCallbacks: AppsAdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_stacktraces, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
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
            holder.trace.text = stackTraces[position].trace
            holder.timestamp.text = stackTraces[position].timestamp.toDate()
        } else if (holder is Header) {
            holder.settings.setOnClickListener {
                appsAdapterCallbacks?.onSettingsPressed(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return stackTraces.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else {
            RecyclerViewConstants.TYPE_ITEM
        }
    }

    fun setOnItemClickListener(appsAdapterCallbacks: AppsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val timestamp: TypeFaceTextView = itemView.findViewById(R.id.timestamp)
        val trace: TypeFaceTextView = itemView.findViewById(R.id.trace)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
    }
}