package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.trackers.reflector.ClassesNamesList
import app.simple.inure.util.AdapterUtils

class AdapterTrackers(private val list: ClassesNamesList, private val keyword: String) : RecyclerView.Adapter<AdapterTrackers.Holder>() {

    private var trackersCallbacks: TrackersCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trackers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.className.text = list.classNames[position]

        holder.className.setOnClickListener {
            trackersCallbacks?.onTrackersClicked(list.classNames[position])
        }

        holder.className.setOnLongClickListener {
            trackersCallbacks?.onTrackersLongClicked(list.classNames[position])
            true
        }

        if (keyword.isNotBlank()) AdapterUtils.searchHighlighter(holder.className, keyword)
    }

    override fun getItemCount(): Int {
        return list.classNames.size
    }

    fun setOnTrackersClickListener(trackersCallbacks: TrackersCallbacks) {
        this.trackersCallbacks = trackersCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val className: DynamicRippleTextView = itemView.findViewById(R.id.adapter_tracker_class_name)
    }

    interface TrackersCallbacks {
        fun onTrackersClicked(className: String)
        fun onTrackersLongClicked(className: String)
    }
}