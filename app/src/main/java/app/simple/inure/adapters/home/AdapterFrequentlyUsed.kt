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
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.models.PackageStats
import java.util.concurrent.TimeUnit.MILLISECONDS

class AdapterFrequentlyUsed : RecyclerView.Adapter<VerticalListViewHolder>() {

    var apps = arrayListOf<PackageStats>()
    private lateinit var appsAdapterCallbacks: AppsAdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_most_used, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_recently_installed, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
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
                appsAdapterCallbacks.onAppLongPressed(apps[position].packageInfo!!, holder.icon)
                true
            }
        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)

            holder.search.setOnClickListener {
                appsAdapterCallbacks.onSearchPressed(it)
            }

            holder.settings.setOnClickListener {
                appsAdapterCallbacks.onSettingsPressed(it)
            }
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is AdapterRecentlyInstalled.Holder) {
            GlideApp.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
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

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
        val search: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_search_button)
    }
}