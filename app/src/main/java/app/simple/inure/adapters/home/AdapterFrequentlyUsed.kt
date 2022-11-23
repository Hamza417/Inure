package app.simple.inure.adapters.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.PackageStats
import app.simple.inure.util.RecyclerViewUtils
import java.util.concurrent.TimeUnit.MILLISECONDS

class AdapterFrequentlyUsed : RecyclerView.Adapter<VerticalListViewHolder>() {

    var apps = arrayListOf<PackageStats>()
    private lateinit var adapterCallbacks: AdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_most_used, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
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
            holder.icon.transitionName = apps[position].packageInfo?.packageName
            holder.icon.loadAppIcon(apps[position].packageInfo!!.packageName, apps[position].packageInfo!!.applicationInfo.enabled)
            holder.name.text = apps[position].packageInfo?.applicationInfo!!.name
            holder.packageId.text = apps[position].packageInfo?.packageName

            holder.name.setStrikeThru(apps[position].packageInfo?.applicationInfo?.enabled ?: false)

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
                adapterCallbacks.onAppClicked(apps[position].packageInfo!!, holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks.onAppLongPressed(apps[position].packageInfo!!, holder.icon)
                true
            }
        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)
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
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_recently_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val date: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_date)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_recently_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
    }
}