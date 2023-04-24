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
import app.simple.inure.glide.util.ImageLoader.loadIconFromServiceInfo
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AdapterTrackers(private val list: ArrayList<Any>, private val keyword: String) : RecyclerView.Adapter<AdapterTrackers.Holder>() {

    private var trackersCallbacks: TrackersCallbacks? = null
    private var isRoot = ConfigurationPreferences.isUsingRoot() || (ConfigurationPreferences.isUsingShizuku() && DevelopmentPreferences.get(DevelopmentPreferences.shizukuTrackerBlocker))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trackers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (list[position] is ActivityInfoModel) {
            holder.icon.loadIconFromActivityInfo((list[position] as ActivityInfoModel).activityInfo)
            holder.name.text = (list[position] as ActivityInfoModel).name.substring((list[position] as ActivityInfoModel).name.lastIndexOf(".") + 1)
            holder.packageId.text = (list[position] as ActivityInfoModel).name
            holder.trackerId.text = (list[position] as ActivityInfoModel).trackerId
            holder.switch.staticChecked((list[position] as ActivityInfoModel).isBlocked)
        } else {
            holder.icon.loadIconFromServiceInfo((list[position] as ServiceInfoModel).serviceInfo)
            holder.name.text = (list[position] as ServiceInfoModel).name.substring((list[position] as ServiceInfoModel).name.lastIndexOf(".") + 1)
            holder.packageId.text = (list[position] as ServiceInfoModel).name
            holder.trackerId.text = (list[position] as ServiceInfoModel).trackerId
            holder.switch.staticChecked((list[position] as ServiceInfoModel).isBlocked)
        }

        if (isRoot) {
            holder.switch.setOnSwitchCheckedChangeListener {
                trackersCallbacks?.onTrackersClicked(list[position], it, position)
            }

            holder.container.setOnClickListener {
                trackersCallbacks?.onTrackersClicked(list[position], holder.switch.isChecked().invert(), position)
            }

            holder.switch.visible(animate = false)
        } else {
            holder.switch.gone()
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
            kotlin.runCatching {
                (list[it.second] as ActivityInfoModel).isEnabled = it.first.isEnabled
                notifyItemChanged(it.second)
            }
        }
    }

    fun updateServiceInfo(it: Pair<ServiceInfoModel, Int>?) {
        it?.let {
            kotlin.runCatching {
                (list[it.second] as ServiceInfoModel).isEnabled = it.first.isEnabled
                notifyItemChanged(it.second)
            }
        }
    }

    fun getTrackers(): Set<String> {
        val trackers = mutableSetOf<String>()

        list.forEach {
            if (it is ActivityInfoModel) {
                trackers.add(it.name)
            } else {
                trackers.add((it as ServiceInfoModel).name)
            }
        }

        return trackers
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
        fun onTrackersClicked(any: Any, enabled: Boolean, position: Int)
    }
}