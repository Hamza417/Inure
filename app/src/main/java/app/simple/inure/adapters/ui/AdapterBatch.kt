package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.models.Tag
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.ArrayUtils.move
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.DateUtils
import app.simple.inure.util.FileUtils.toFileOrNull
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.Sort
import app.simple.inure.util.SortBatch.getSortedList
import app.simple.inure.util.StatusBarHeight
import com.bumptech.glide.Glide
import java.util.stream.Collectors

class AdapterBatch(var apps: ArrayList<BatchPackageInfo>, var headerEnabled: Boolean = true) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterCallbacks: AdapterCallbacks? = null
    private val pattern = FormattingPreferences.getDateFormat()
    private var highlight = BatchPreferences.isSelectedBatchHighlighted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                if (LocaleUtils.isAppRussianLocale() && StatusBarHeight.isLandscape(parent.context).invert()) {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_batch_ru, parent, false))
                } else {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_batch, parent, false))
                }
            }

            RecyclerViewUtils.TYPE_ITEM -> {
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
            holder.icon.loadAppIcon(apps[position].packageInfo.packageName,
                                    apps[position].packageInfo.safeApplicationInfo.enabled,
                                    apps[position].packageInfo.safeApplicationInfo.sourceDir.toFileOrNull())
            holder.name.text = apps[position].packageInfo.safeApplicationInfo.name
            holder.packageId.text = apps[position].packageInfo.packageName

            holder.name.setAppVisualStates(apps[position].packageInfo)
            holder.checkBox.setChecked(apps[position].isSelected, false)

            if (highlight) {
                holder.container.setDefaultBackground(apps[position].isSelected)
            } else {
                holder.container.setDefaultBackground(false)
            }

            holder.checkBox.setOnCheckedChangeListener { isChecked ->
                apps[position].isSelected = isChecked
                apps[position].dateSelected = if (isChecked) System.currentTimeMillis() else -1
                adapterCallbacks?.onBatchChanged(apps[position])

                if (highlight) {
                    holder.container.setDefaultBackground(apps[position].isSelected)
                }

                if (apps[position].isSelected) {
                    holder.date.text = holder.itemView.context.getString(
                            R.string.selected_on,
                            DateUtils.formatDate(apps[position].dateSelected, pattern))
                } else {
                    holder.date.setText(R.string.not_selected)
                }

                if (BatchPreferences.isSelectionOnTop() && isChecked) {
                    if (headerEnabled) {
                        val selectedApps: ArrayList<BatchPackageInfo> = apps.stream()
                            .filter { it.isSelected }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>
                        var index = 0

                        selectedApps.getSortedList(BatchPreferences.getSortStyle(), BatchPreferences.isReverseSorting())

                        for (i in selectedApps.indices) {
                            if (selectedApps[i].packageInfo.packageName == apps[position].packageInfo.packageName) {
                                index = i
                                break
                            }
                        }

                        apps.move(holder.bindingAdapterPosition.minus(1), index)
                        notifyItemMoved(holder.bindingAdapterPosition, index.plus(1))
                        notifyItemChanged(holder.bindingAdapterPosition)
                        notifyItemChanged(index.plus(1))
                        notifyItemRangeChanged(0, apps.size.plus(1))
                    } else {
                        apps.move(holder.bindingAdapterPosition, 0)
                        notifyItemMoved(holder.bindingAdapterPosition, 0)
                        notifyItemRangeChanged(0, apps.size)
                    }
                }

                if (headerEnabled) {
                    notifyItemChanged(0)
                }
            }

            if (apps[position].isSelected) {
                holder.date.text = holder.itemView.context.getString(
                        R.string.selected_on,
                        DateUtils.formatDate(apps[position].dateSelected, pattern))
            } else {
                holder.date.setText(R.string.not_selected)
            }

            holder.container.setOnClickListener {
                holder.checkBox.animateToggle()
                // appsAdapterCallbacks?.onAppClicked(apps[position].packageInfo, holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onAppLongPressed(apps[position].packageInfo, holder.icon)
                true
            }
        }

        if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), apps.size)
            holder.selected.text = holder.getString(R.string.selected_apps, getSelectedAppsCount())

            holder.category.text = when (BatchPreferences.getAppsCategory()) {
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

            holder.sorting.text = when (BatchPreferences.getSortStyle()) {
                Sort.NAME -> {
                    holder.getString(R.string.name)
                }

                Sort.PACKAGE_NAME -> {
                    holder.getString(R.string.package_name)
                }

                Sort.INSTALL_DATE -> {
                    holder.getString(R.string.install_date)
                }

                Sort.SIZE -> {
                    holder.getString(R.string.app_size)
                }

                Sort.UPDATE_DATE -> {
                    holder.getString(R.string.update_date)
                }

                Sort.TARGET_SDK -> {
                    holder.getString(R.string.target_sdk)
                }

                Sort.MIN_SDK -> {
                    holder.getString(R.string.minimum_sdk)
                }

                else -> {
                    holder.getString(R.string.unknown)
                }
            }
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            Glide.with(holder.icon).clear(holder.icon)
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
                RecyclerViewUtils.TYPE_HEADER
            } else RecyclerViewUtils.TYPE_ITEM
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    fun getCurrentAppsList(): ArrayList<BatchPackageInfo> {
        return apps.stream().filter { it.isSelected }
            .collect(Collectors.toList()) as ArrayList<BatchPackageInfo>
    }

    @Suppress("unused")
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
                it.packageInfo.safeApplicationInfo.name
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

    @Suppress("unused")
    fun updateList(it: ArrayList<BatchPackageInfo>) {
        val oldSize: Int = apps.size
        apps.clear()
        notifyItemRangeRemoved(1, oldSize.plus(1))
        apps.addAll(it)
        notifyItemRangeInserted(1, it.size.plus(1))
    }

    fun selectAll() {
        for (i in apps.indices) {
            apps[i].isSelected = true
            notifyItemChanged(i.plus(1))
        }
    }

    fun isAllSelected(): Boolean {
        return apps.stream().allMatch { it.isSelected }
    }

    private fun deselectAll() {
        for (i in apps.indices) {
            apps[i].isSelected = false
            notifyItemChanged(i.plus(1))
        }

        notifyItemChanged(0) // header
    }

    fun getSelectedAppsCount(): Int {
        return apps.count { it.isSelected }
    }

    fun createSelectionFromTags(tag: Tag) {
        deselectAll()
        tag.packages.split(",").forEach { packageName ->
            for (i in apps.indices) {
                if (apps[i].packageInfo.packageName == packageName) {
                    apps[i].isSelected = true
                    notifyItemChanged(i.plus(1))
                }
            }
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val date: TypeFaceTextView = itemView.findViewById(R.id.date)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
        val selected: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_selected)
    }
}
