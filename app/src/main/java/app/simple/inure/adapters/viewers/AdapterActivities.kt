package app.simple.inure.adapters.viewers

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.condensed.CondensedConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AdapterActivities(private val packageInfo: PackageInfo, private val activities: MutableList<ActivityInfoModel>, val keyword: String)
    : RecyclerView.Adapter<AdapterActivities.Holder>() {

    private lateinit var activitiesCallbacks: ActivitiesCallbacks
    private val isRootMode = ConfigurationPreferences.isUsingRoot() // || ConfigurationPreferences.isUsingShizuku()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activities, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = activities[holder.absoluteAdapterPosition].name.substring(activities[holder.absoluteAdapterPosition].name.lastIndexOf(".") + 1)
        holder.activityPackageID.text = activities[position].name
        holder.icon.loadIconFromActivityInfo(activities[position].activityInfo)
        holder.status.text = holder.itemView.context.getString(
                R.string.activity_status,

                if (activities[position].exported) {
                    holder.itemView.context.getString(R.string.exported)
                } else {
                    holder.itemView.context.getString(R.string.not_exported)
                },

                kotlin.runCatching {
                    if (ActivityUtils.isEnabled(holder.itemView.context, packageInfo.packageName, activities[position].name)) {
                        holder.itemView.context.getString(R.string.enabled)
                    } else {
                        holder.itemView.context.getString(R.string.disabled)
                    }
                }.getOrElse {
                    holder.itemView.context.getString(R.string.no_state)
                }
        )
        holder.status.append(activities[position].status)
        holder.name.setTrackingIcon(activities[position].trackerId.isNullOrEmpty().not())

        holder.launch.setOnClickListener {
            activitiesCallbacks.onLaunchClicked(packageInfo.packageName, activities[holder.absoluteAdapterPosition].name)
        }

        if (activities[position].exported) {
            kotlin.runCatching {
                if (ActivityUtils.isEnabled(holder.itemView.context, packageInfo.packageName, activities[holder.absoluteAdapterPosition].name)) {
                    holder.launch.visible(false)
                    holder.divider.visible(false)
                } else {
                    holder.launch.gone()
                    holder.divider.gone()
                }
            }.onFailure {
                holder.launch.gone()
                holder.divider.gone()
            }
        } else {
            holder.launch.gone()
            holder.divider.gone()
        }

        holder.container.setOnClickListener {
            activitiesCallbacks.onActivityClicked(activities[holder.absoluteAdapterPosition], activities[holder.absoluteAdapterPosition].name)
        }

        holder.container.setOnLongClickListener {
            activitiesCallbacks
                .onActivityLongPressed(
                        activities[holder.absoluteAdapterPosition],
                        packageInfo,
                        it,
                        holder.absoluteAdapterPosition)
            true
        }

        if (keyword.isNotBlank()) {
            AdapterUtils.searchHighlighter(holder.name, keyword)
        }
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.adapter_activity_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_activity_name)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_activity_status)
        val activityPackageID: TypeFaceTextView = itemView.findViewById(R.id.adapter_activity_package)
        val divider: View = itemView.findViewById(R.id.divider)
        val launch: View = itemView.findViewById(R.id.adapter_activity_launch_button)
        val container: CondensedConstraintLayout = itemView.findViewById(R.id.adapter_activity_container)

        init {
            name.enableSelection()
            activityPackageID.enableSelection()
        }
    }

    fun setOnActivitiesCallbacks(activitiesCallbacks: ActivitiesCallbacks) {
        this.activitiesCallbacks = activitiesCallbacks
    }

    companion object {
        // private const val intentMain = "android.intent.action.MAIN"
        // private const val categoryLauncher = "android.intent.category.LAUNCHER"
        // private const val categoryLeanback = "android.intent.category.LEANBACK_LAUNCHER"

        interface ActivitiesCallbacks {
            fun onActivityClicked(activityInfoModel: ActivityInfoModel, packageId: String)
            fun onActivityLongPressed(activityInfoModel: ActivityInfoModel, packageInfo: PackageInfo, icon: View, position: Int)
            fun onLaunchClicked(packageName: String, name: String)
        }
    }
}
