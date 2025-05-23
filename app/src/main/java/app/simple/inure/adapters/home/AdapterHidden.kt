package app.simple.inure.adapters.home

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.RecyclerViewUtils
import com.bumptech.glide.Glide

class AdapterHidden(var apps: ArrayList<PackageInfo> = arrayListOf()) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterCallbacks: AdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_hidden, parent, false))
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
            holder.icon.transitionName = apps[position].packageName
            holder.icon.loadAppIcon(apps[position].packageName, apps[position].safeApplicationInfo.enabled)
            holder.name.text = apps[position].safeApplicationInfo.name
            holder.packageId.text = apps[position].packageName

            holder.name.setStrikeThru(apps[position].safeApplicationInfo.enabled)
            holder.name.setFOSSIcon(FOSSParser.isPackageFOSS(apps[position]))

            holder.date.text = apps[position].getApplicationInstallTime(holder.itemView.context, FormattingPreferences.getDateFormat())

            holder.container.setOnClickListener {
                adapterCallbacks.onAppClicked(apps[position], holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks.onAppLongPressed(apps[position], holder.icon)
                true
            }
        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            Glide.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return apps.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else RecyclerViewUtils.TYPE_ITEM
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.adapter_recently_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_app_package_id)
        val date: TypeFaceTextView = itemView.findViewById(R.id.adapter_recently_date)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_recently_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
    }
}
