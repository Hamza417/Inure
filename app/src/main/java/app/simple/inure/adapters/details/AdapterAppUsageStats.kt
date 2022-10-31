package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.PackageStats
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.RecyclerViewUtils
import java.util.concurrent.TimeUnit

class AdapterAppUsageStats(private val packageStats: PackageStats) : RecyclerView.Adapter<VerticalListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_app_usage_stats, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_app_usage_stats, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        @Suppress("NAME_SHADOWING") val position = position - 1

        if (holder is Holder) {
            holder.date.text = packageStats.appUsage!![position].date.toDate()

            with(packageStats.appUsage!![position].startTime) {
                holder.time.apply {
                    this.text = when {
                        TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_seconds,
                                                   TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_short,
                                                   TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                            this.context.getString(R.string.used_for_long,
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                        else -> {
                            this.context.getString(R.string.used_for_days,
                                                   TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                    }
                }
            }
        } else if (holder is Header) {
            with(System.currentTimeMillis() - packageStats.appUsage!![0].date) {
                holder.lastUsed.apply {
                    this.text = when {
                        TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                            this.context.getString(R.string.last_used_seconds,
                                                   TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                            this.context.getString(R.string.last_used_short,
                                                   TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                            this.context.getString(R.string.last_used_long,
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                        else -> {
                            this.context.getString(R.string.last_used_days,
                                                   TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                    }
                }
            }

            with(packageStats.totalTimeUsed) {
                holder.screenTime.apply {
                    this.text = when {
                        TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_seconds,
                                                   TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_short,
                                                   TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                            this.context.getString(R.string.used_for_long,
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                        else -> {
                            this.context.getString(R.string.used_for_days,
                                                   TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                    }
                }
            }

            holder.launchCount.text = holder.context.getString(R.string.times, packageStats.launchCount)
            holder.mobileData.text = packageStats.mobileData.toString()
            holder.wifiData.text = packageStats.wifiData.toString()
        }
    }

    override fun getItemCount(): Int {
        return if (packageStats.appUsage?.size?.isNotZero() == true) {
            packageStats.appUsage?.size?.plus(1) ?: 0
        } else {
            0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val screenTime: TypeFaceTextView = itemView.findViewById(R.id.screen_time)
        val launchCount: TypeFaceTextView = itemView.findViewById(R.id.launch_count)
        val lastUsed: TypeFaceTextView = itemView.findViewById(R.id.last_used)
        val mobileData: TypeFaceTextView = itemView.findViewById(R.id.mobile_data)
        val wifiData: TypeFaceTextView = itemView.findViewById(R.id.wifi_data)
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val date: TypeFaceTextView = itemView.findViewById(R.id.date)
        val time: TypeFaceTextView = itemView.findViewById(R.id.time)
    }
}