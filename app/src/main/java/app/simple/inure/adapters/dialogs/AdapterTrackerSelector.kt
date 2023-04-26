package app.simple.inure.adapters.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.Tracker

class AdapterTrackerSelector(private val trackers: ArrayList<Tracker>, private val selectedPaths: ArrayList<Tracker>) : RecyclerView.Adapter<AdapterTrackerSelector.Holder>() {

    private var trackerSelectorCallbacks: TrackerSelectorCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_selector_split_apk, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.path.text = trackers[position].name
        holder.checkBox.setChecked(selectedPaths.contains(trackers[position]))

        holder.checkBox.setOnCheckedChangeListener { isChecked ->
            trackerSelectorCallbacks?.onTrackerSelected(trackers[position], isChecked)
        }

        holder.container.setOnClickListener {
            holder.checkBox.toggle()
        }
    }

    override fun getItemCount(): Int {
        return trackers.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val checkBox: InureCheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.container)
    }

    fun setTrackerSelectorCallbacks(trackerSelectorCallbacks: TrackerSelectorCallbacks) {
        this.trackerSelectorCallbacks = trackerSelectorCallbacks
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        notifyDataSetChanged()
    }

    companion object {
        interface TrackerSelectorCallbacks {
            fun onTrackerSelected(tracker: Tracker, isChecked: Boolean)
        }
    }
}