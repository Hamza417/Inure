package app.simple.inure.adapters.batch

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.glide.util.ImageLoader.loadIconFromServiceInfo
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

        holder.name.text = tracker.name

        holder.path.text = when {
            tracker.isActivity -> {
                holder.icon.loadIconFromActivityInfo(tracker.activityInfo)
                tracker.activityInfo.packageName + "/" + tracker.activityInfo.name
            }
            tracker.isService -> {
                holder.icon.loadIconFromServiceInfo(tracker.serviceInfo)
                tracker.serviceInfo.packageName + "/" + tracker.serviceInfo.name
            }
            tracker.isReceiver -> {
                holder.icon.loadIconFromActivityInfo(tracker.activityInfo)
                tracker.activityInfo.packageName + "/" + tracker.activityInfo.name
            }
            else -> holder.itemView.context.getString(R.string.unknown)
        }

        holder.path.text = holder.path.text.optimizeToColoredString("/")

        holder.details.text = buildString {
            append(tracker.codeSignature)
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

        holder.checkBox.isChecked = tracker.isBlocked.invert()

        holder.container.setOnClickListener {
            tracker.isBlocked = !tracker.isBlocked
            holder.checkBox.toggle()
        }
    }

    override fun getItemCount(): Int {
        return trackers.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        trackers.forEach {
            it.isBlocked = false // contextually unblock all
        }

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
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

    fun getSelectedPackages(): java.util.ArrayList<Tracker> {
        val selectedPackages = java.util.ArrayList<Tracker>()

        trackers.forEach {
            if (!it.isBlocked) {
                selectedPackages.add(it)
            }
        }

        return selectedPackages
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val path: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val details: TypeFaceTextView = itemView.findViewById(R.id.details)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
    }
}