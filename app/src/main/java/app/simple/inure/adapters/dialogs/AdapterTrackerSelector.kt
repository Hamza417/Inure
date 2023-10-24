package app.simple.inure.adapters.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.Tracker
import app.simple.inure.util.StringUtils.optimizeToColoredString

class AdapterTrackerSelector(private val trackers: ArrayList<Tracker>, private val selectedPaths: ArrayList<Tracker>) : RecyclerView.Adapter<AdapterTrackerSelector.Holder>() {

    private var trackerSelectorCallbacks: TrackerSelectorCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_selector_tracker, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val tracker = trackers[position]

        holder.path.text = when {
            tracker.isActivity -> tracker.activityInfo.packageName + "/" + tracker.activityInfo.name
            tracker.isService -> tracker.serviceInfo.packageName + "/" + tracker.serviceInfo.name
            tracker.isReceiver -> tracker.activityInfo.packageName + "/" + tracker.activityInfo.name
            else -> holder.itemView.context.getString(R.string.unknown)
        }

        holder.path.text = holder.path.text.optimizeToColoredString("/")

        holder.tracker.text = buildString {
            append(tracker.trackerId)
            append(" | ")
            when {
                tracker.isActivity -> {
                    append(holder.itemView.context.getString(R.string.activity))
                }
                tracker.isService -> {
                    append(holder.itemView.context.getString(R.string.service))
                }
                tracker.isReceiver -> {
                    append(holder.itemView.context.getString(R.string.receiver))
                }
                else -> {
                    append(holder.itemView.context.getString(R.string.unknown))
                }
            }
        }

        holder.checkBox.setCheckedWithoutAnimations(selectedPaths.contains(trackers[position]))

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
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val tracker: TypeFaceTextView = itemView.findViewById(R.id.tracker)
        val checkBox: InureCheckBox = itemView.findViewById(R.id.checkbox)
    }

    fun setTrackerSelectorCallbacks(trackerSelectorCallbacks: TrackerSelectorCallbacks) {
        this.trackerSelectorCallbacks = trackerSelectorCallbacks
    }

    companion object {
        interface TrackerSelectorCallbacks {
            fun onTrackerSelected(tracker: Tracker, isChecked: Boolean)
        }
    }
}