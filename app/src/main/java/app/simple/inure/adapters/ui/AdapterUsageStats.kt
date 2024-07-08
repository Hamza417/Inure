package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.AdapterUtils.setInfoStates
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.SortUsageStats
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.util.ViewUtils.visible
import java.util.concurrent.TimeUnit

class AdapterUsageStats(private val apps: ArrayList<PackageStats>) : RecyclerView.Adapter<VerticalListViewHolder>(), PopupTextProvider {

    private var adapterCallbacks: AdapterCallbacks? = null
    private var isLimitedToHours = StatisticsPreferences.isLimitToHours()
    private var isLoader = false // list.isEmpty()
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                if (LocaleUtils.isAppRussianLocale() && StatusBarHeight.isLandscape(parent.context).invert()) {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_usage_stats_ru, parent, false))
                } else {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_usage_stats, parent, false))
                }
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_usage_stats, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            holder.icon.transitionName = apps[position].packageInfo?.packageName
            holder.icon.loadAppIcon(apps[position].packageInfo!!.packageName, apps[position].packageInfo!!.applicationInfo.enabled)
            holder.name.text = apps[position].packageInfo!!.applicationInfo.name
            holder.mobileData.text = buildString {
                appendFlag(apps[position].mobileData?.tx?.toSize())
                appendFlag(apps[position].mobileData?.rx?.toSize())
            }

            holder.wifi.text = buildString {
                appendFlag(apps[position].wifiData?.tx?.toSize())
                appendFlag(apps[position].wifiData?.rx?.toSize())
            }

            holder.name.setInfoStates(apps[position].packageInfo!!)

            with(apps[position].totalTimeUsed) {
                holder.time.apply {
                    this.text = buildString {
                        append(
                                when {
                                    TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                                        this@apply.context.getString(R.string.used_for_seconds,
                                                                     TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                                    }
                                    TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                                        this@apply.context.getString(R.string.used_for_short,
                                                                     TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                                    }
                                    TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                                        this@apply.context.getString(R.string.used_for_long,
                                                                     TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                                     (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                    }
                                    else -> {
                                        if (isLimitedToHours) {
                                            this@apply.context.getString(R.string.used_for_long,
                                                                         TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                                         (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                        } else {
                                            this@apply.context.getString(R.string.used_for_days,
                                                                         TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                                         (TimeUnit.MILLISECONDS.toHours(this@with) % 24).toString(),
                                                                         (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                        }
                                    }
                                })

                        append(" | ")
                        append(apps[position].appSize.toSize())
                    }
                }
            }

            holder.container.setOnClickListener {
                adapterCallbacks?.onAppClicked(apps[position].packageInfo!!, holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onAppLongPressed(apps[position].packageInfo!!, holder.icon)
                true
            }

        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)

            holder.category.text = when (StatisticsPreferences.getAppsCategory()) {
                SortConstant.USER -> {
                    holder.getString(R.string.user)
                }
                SortConstant.SYSTEM -> {
                    holder.getString(R.string.system)
                }
                SortConstant.BOTH -> {
                    with(StringBuilder()) {
                        append(holder.getString(R.string.user))
                        append(" | ")
                        append(holder.getString(R.string.system))
                    }
                }
                else -> {
                    holder.getString(R.string.unknown)
                }
            }

            holder.sort.text = when (StatisticsPreferences.getSortedBy()) {
                SortUsageStats.NAME -> {
                    holder.getString(R.string.name)
                }
                SortUsageStats.DATA_RECEIVED -> {
                    holder.getString(R.string.data_received)
                }
                SortUsageStats.DATA_SENT -> {
                    holder.getString(R.string.data_sent)
                }
                SortUsageStats.WIFI_RECEIVED -> {
                    holder.getString(R.string.wifi_received)
                }
                SortUsageStats.WIFI_SENT -> {
                    holder.getString(R.string.wifi_sent)
                }
                SortUsageStats.TIME_USED -> {
                    holder.getString(R.string.time_used)
                }
                SortUsageStats.APP_SIZE -> {
                    holder.getString(R.string.app_size)
                }
                SortUsageStats.PACKAGE_NAME -> {
                    holder.getString(R.string.package_name)
                }
                SortUsageStats.INSTALL_DATE -> {
                    holder.getString(R.string.install_date)
                }
                SortUsageStats.UPDATE_DATE -> {
                    holder.getString(R.string.update_date)
                }
                SortUsageStats.TARGET_SDK -> {
                    holder.getString(R.string.target_sdk)
                }
                SortUsageStats.MINIMUM_SDK -> {
                    holder.getString(R.string.minimum_sdk)
                }
                else -> {
                    holder.getString(R.string.unknown)
                }
            }

            if (isLoader) holder.loader.visible(false)
        }
    }

    override fun getItemCount(): Int {
        return apps.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            GlideApp.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getPopupText(position: Int): String {
        return apps[position].packageInfo?.applicationInfo?.name?.substring(0, 1) ?: ""
    }

    fun setOnStatsCallbackListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    fun notifyAllData() {
        isLimitedToHours = StatisticsPreferences.isLimitToHours()
        for (i in apps.indices) {
            notifyItemChanged(i.plus(1))
        }
    }

    fun enableLoader() {
        isLoader = true
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_usage_stats_container)
        val icon: AppIconImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val time: TypeFaceTextView = itemView.findViewById(R.id.total_time_used)
        val mobileData: TypeFaceTextView = itemView.findViewById(R.id.mobile_data)
        val wifi: TypeFaceTextView = itemView.findViewById(R.id.wifi)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
        val sort: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val loader: CustomProgressBar = itemView.findViewById(R.id.loader)
    }
}
