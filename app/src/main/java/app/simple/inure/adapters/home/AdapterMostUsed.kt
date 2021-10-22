package app.simple.inure.adapters.home

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.model.PackageStats
import app.simple.inure.preferences.ConfigurationPreferences
import java.util.concurrent.TimeUnit.*

class AdapterMostUsed : RecyclerView.Adapter<AdapterMostUsed.Holder>() {

    var apps = arrayListOf<PackageStats>()
    private lateinit var appsAdapterCallbacks: AppsAdapterCallbacks

    private val pattern = ConfigurationPreferences.getDateFormat()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context)
                              .inflate(R.layout.adapter_recently_installed, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.icon.transitionName = "recently_app_$position"
        holder.icon.loadAppIcon(apps[position].packageInfo!!.packageName)
        holder.name.text = apps[position].packageInfo?.applicationInfo!!.name
        holder.packageId.text = apps[position].packageInfo?.packageName

        if (apps[position].packageInfo?.applicationInfo!!.enabled) {
            holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        } else {
            holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        with(apps[position].totalTimeUsed) {
            holder.date.apply {
                this.text = when {
                    MILLISECONDS.toSeconds(this@with) < 60 -> {
                        this.context.getString(R.string.used_for_seconds,
                                               MILLISECONDS.toSeconds(this@with).toString())
                    }
                    MILLISECONDS.toMinutes(this@with) < 60 -> {
                        this.context.getString(R.string.used_for_short,
                                               MILLISECONDS.toMinutes(this@with).toString())
                    }
                    else -> {
                        this.context.getString(R.string.used_for_long,
                                               MILLISECONDS.toHours(this@with).toString(),
                                               (MILLISECONDS.toMinutes(this@with) % 60).toString())
                    }
                }
            }
        }

        holder.container.setOnClickListener {
            appsAdapterCallbacks.onAppClicked(apps[position].packageInfo!!, holder.icon)
        }

        holder.container.setOnLongClickListener {
            appsAdapterCallbacks.onAppLongPress(apps[position].packageInfo!!, it, holder.icon, position)
            true
        }
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        GlideApp.with(holder.icon).clear(holder.icon)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnItemClickListener(appsAdapterCallbacks: AppsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_recently_app_icon)
        val name: TextView = itemView.findViewById(R.id.adapter_recently_app_name)
        val packageId: TextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val date: TextView = itemView.findViewById(R.id.adapter_recently_date)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_recently_container)
    }
}