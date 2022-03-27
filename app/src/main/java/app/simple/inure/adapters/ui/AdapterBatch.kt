package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.CheckBox
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.ArrayUtils.move
import app.simple.inure.util.DateUtils
import java.util.stream.Collectors

class AdapterBatch(var apps: ArrayList<BatchPackageInfo>, var headerEnabled: Boolean = true) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var appsAdapterCallbacks: AppsAdapterCallbacks? = null
    private val pattern = FormattingPreferences.getDateFormat()
    private var highlight = BatchPreferences.isSelectedBatchHighlighted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_batch, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_batch, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = if (headerEnabled) position_ - 1 else position_

        if (holder is Holder) {
            holder.icon.transitionName = "app_$position"
            holder.icon.loadAppIcon(apps[position].packageInfo.packageName)
            holder.name.text = apps[position].packageInfo.applicationInfo.name
            holder.packageId.text = apps[position].packageInfo.packageName

            if (apps[position].packageInfo.applicationInfo.enabled) {
                holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            } else {
                holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            holder.checkBox.setCheckedWithoutAnimations(apps[position].isSelected)

            if (highlight) {
                holder.container.setDefaultBackground(apps[position].isSelected)
            } else {
                holder.container.setDefaultBackground(false)
            }

            holder.checkBox.setOnCheckedChangeListener {
                apps[position].isSelected = it
                apps[position].dateSelected = if (it) System.currentTimeMillis() else -1
                appsAdapterCallbacks?.onBatchChanged(apps[position])

                if (highlight) {
                    holder.container.setDefaultBackground(apps[position].isSelected)
                }

                if (apps[position].isSelected) {
                    holder.date.text = holder.itemView.context.getString(R.string.selected_on, DateUtils.formatDate(apps[position].dateSelected, pattern))
                } else {
                    holder.date.setText(R.string.not_selected)
                }

                if (BatchPreferences.isSelectionOnTop() && it) {
                    apps.move(position, 0)
                    notifyItemMoved(position_, 1)
                }
            }

            if (apps[position].isSelected) {
                holder.date.text = holder.itemView.context.getString(R.string.selected_on, DateUtils.formatDate(apps[position].dateSelected, pattern))
            } else {
                holder.date.setText(R.string.not_selected)
            }

            holder.container.setOnClickListener {
                holder.checkBox.invertCheckedStatus()
                // appsAdapterCallbacks?.onAppClicked(apps[position].packageInfo, holder.icon)
            }

            holder.container.setOnLongClickListener {
                appsAdapterCallbacks?.onAppLongPressed(apps[position].packageInfo, holder.icon)
                true
            }
        }

        if (holder is Header) {
            holder.search.setOnClickListener {
                appsAdapterCallbacks?.onSearchPressed(it)
            }

            holder.filter.setOnClickListener {
                appsAdapterCallbacks?.onFilterPressed(it)
            }

            holder.sort.setOnClickListener {
                appsAdapterCallbacks?.onSortPressed(it)
            }

            holder.settings.setOnClickListener {
                appsAdapterCallbacks?.onSettingsPressed(it)
            }

            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            GlideApp.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return if (headerEnabled) apps.size + 1 else apps.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (headerEnabled) {
            if (position == 0) {
                RecyclerViewConstants.TYPE_HEADER
            } else RecyclerViewConstants.TYPE_ITEM
        } else {
            RecyclerViewConstants.TYPE_ITEM
        }
    }

    fun setOnItemClickListener(appsAdapterCallbacks: AppsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks
    }

    fun getCurrentAppsList(): ArrayList<BatchPackageInfo> {
        return apps.stream().filter { it.isSelected }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>
    }

    fun updateBatchItem(batchPackageInfo: BatchPackageInfo) {
        for (i in apps.indices) {
            if (apps[i].packageInfo.packageName == batchPackageInfo.packageInfo.packageName) {
                apps[i] = batchPackageInfo
                notifyItemChanged(i.plus(1))
                break
            }
        }
    }

    fun moveSelectedItemsToTheTop() {
        if (BatchPreferences.isSelectionOnTop()) {
            apps.sortByDescending {
                it.isSelected
            }
        } else {
            apps.sortBy {
                it.packageInfo.applicationInfo.name
            }
        }
        for (i in apps.indices) notifyItemChanged(i.plus(1))
    }

    fun updateSelectionsHighlights(highlight: Boolean) {
        this.highlight = highlight

        for (i in apps.indices) {
            if (apps[i].isSelected) {
                notifyItemChanged(i.plus(1))
            }
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.adapter_batch_app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_batch_app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.adapter_batch_app_package_id)
        val date: TypeFaceTextView = itemView.findViewById(R.id.adapter_batch_date)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_batch_app_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sort: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_sort_button)
        val filter: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_filter_button)
        val search: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_search_button)
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
    }
}