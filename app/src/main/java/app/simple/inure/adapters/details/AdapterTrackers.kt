package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.glide.util.ImageLoader.loadIconFromServiceInfo
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ViewUtils.visible

class AdapterTrackers(private val list: ArrayList<Tracker>, private val keyword: String) : RecyclerView.Adapter<AdapterTrackers.Holder>() {

    private var trackersCallbacks: TrackersCallbacks? = null
    private var isRoot = ConfigurationPreferences.isUsingRoot()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_trackers, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        when {
            list[position].isActivity -> {
                holder.icon.loadIconFromActivityInfo(list[position].activityInfo)
                holder.name.text = list[position].name
                holder.packageId.text = list[position].componentName
                holder.trackerId.text = buildString {
                    append(list[position].codeSignature)
                    append(" | ")
                    append(holder.itemView.context.getString(R.string.activity))
                }

                holder.switch.isChecked = list[position].isBlocked.invert()
            }
            list[position].isService -> {
                holder.icon.loadIconFromServiceInfo(list[position].serviceInfo)
                holder.name.text = list[position].name
                holder.packageId.text = list[position].componentName
                holder.trackerId.text = buildString {
                    append(list[position].codeSignature)
                    append(" | ")
                    append(holder.itemView.context.getString(R.string.service))
                }

                holder.switch.isChecked = list[position].isBlocked.invert()
            }
            list[position].isReceiver -> {
                holder.icon.loadIconFromActivityInfo(list[position].receiverInfo)
                holder.name.text = list[position].name
                holder.packageId.text = list[position].componentName
                holder.trackerId.text = buildString {
                    append(list[position].codeSignature)
                    append(" | ")
                    append(holder.itemView.context.getString(R.string.receiver))
                }

                holder.switch.isChecked = list[position].isBlocked.invert()
            }
        }

        if (isRoot) {
            holder.switch.setOnSwitchCheckedChangeListener {
                trackersCallbacks?.onTrackersClicked(list[position], it, position)
            }

            holder.container.setOnClickListener {
                trackersCallbacks?.onTrackersClicked(list[position], holder.switch.isChecked.invert(), position)
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

    fun updateTracker(it: Pair<Tracker, Int>) {
        it.let {
            kotlin.runCatching {
                list[it.second].isBlocked = it.first.isBlocked
                notifyItemChanged(it.second)
            }
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val trackerId: TypeFaceTextView = itemView.findViewById(R.id.tracker_id)
        val switch: Switch = itemView.findViewById(R.id.switch_view)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)

        init {
            name.enableSelection()
            packageId.enableSelection()
            trackerId.enableSelection()
        }
    }

    interface TrackersCallbacks {
        fun onTrackersClicked(tracker: Tracker, enabled: Boolean, position: Int)
    }
}