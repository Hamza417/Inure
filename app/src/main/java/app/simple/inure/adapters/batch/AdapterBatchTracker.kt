package app.simple.inure.adapters.batch

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
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.StringUtils.optimizeToColoredString

class AdapterBatchTracker(private val trackers: ArrayList<Tracker>) : RecyclerView.Adapter<AdapterBatchTracker.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_selector_batch_tracker, parent, false))
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

        holder.tracker.text = tracker.trackerId
        holder.checkBox.setCheckedWithoutAnimations(tracker.isBlocked.invert())

        holder.container.setOnClickListener {
            tracker.isBlocked = !tracker.isBlocked
            holder.checkBox.toggle()
        }
    }

    override fun getItemCount(): Int {
        return trackers.size
    }

    fun selectAll() {
        trackers.forEach {
            it.isBlocked = false // contextually unblock all
        }

        notifyDataSetChanged()
    }

    fun unselectAll() {
        trackers.forEach {
            it.isBlocked = true // contextually block all
        }

        notifyDataSetChanged()
    }

    fun isAllSelected(): Boolean {
        trackers.forEach {
            if (it.isBlocked) {
                return false
            }
        }

        return true
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val tracker: TypeFaceTextView = itemView.findViewById(R.id.tracker)
        val checkBox: InureCheckBox = itemView.findViewById(R.id.checkbox)
    }
}