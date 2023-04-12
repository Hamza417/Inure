package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.ConditionUtils.invert

class AdapterTrackers(private val list: ArrayList<ActivityInfoModel>, private val keyword: String) : RecyclerView.Adapter<AdapterTrackers.Holder>() {

    private var trackersCallbacks: TrackersCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trackers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.packageId.text = list[position].name
        holder.trackerId.text = list[position].trackerId
        holder.name.text = list[position].name.substring(list[position].name.lastIndexOf(".") + 1)
        holder.icon.loadIconFromActivityInfo(list[position].activityInfo)
        holder.switch.setChecked(list[position].isEnabled)

        holder.switch.setOnSwitchCheckedChangeListener {
            trackersCallbacks?.onTrackersClicked(list[position], it, position)
        }

        holder.container.setOnClickListener {
            trackersCallbacks?.onTrackersClicked(list[position], holder.switch.isChecked().invert(), position)
        }

        if (keyword.isNotBlank()) AdapterUtils.searchHighlighter(holder.packageId, keyword)
        if (keyword.isNotBlank()) AdapterUtils.searchHighlighter(holder.trackerId, keyword)
        if (keyword.isNotBlank()) AdapterUtils.searchHighlighter(holder.name, keyword)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnTrackersClickListener(trackersCallbacks: TrackersCallbacks) {
        this.trackersCallbacks = trackersCallbacks
    }

    fun updateActivityInfo(it: Pair<ActivityInfoModel, Int>?) {
        it?.let {
            list[it.second].isEnabled = it.first.isEnabled
            notifyItemChanged(it.second)
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val trackerId: TypeFaceTextView = itemView.findViewById(R.id.tracker_id)
        val switch: SwitchView = itemView.findViewById(R.id.switch_view)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    interface TrackersCallbacks {
        fun onTrackersClicked(activityInfoModel: ActivityInfoModel, enabled: Boolean, position: Int)
    }
}