package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.condensed.CondensedConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAPKIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.ApkFile
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.SortApks
import java.util.Locale

class AdapterApks(var paths: ArrayList<ApkFile> = arrayListOf(),
                  private val transitionName: String,
                  private val transitionPosition: Int) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterCallbacks: AdapterCallbacks

    var isSelectionMode: Boolean = paths.any { it.isSelected }
        set(value) {
            if (field != value) {
                field = value
                for (i in paths.indices) {
                    notifyItemChanged(i + 1)
                }
            }
        }
        get() {
            return paths.any { it.isSelected }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_apks, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_apks, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1
        if (holder is Holder) {
            if (transitionName.isNotEmpty()) {
                if (position == transitionPosition) {
                    holder.icon.transitionName = transitionName
                } else {
                    holder.icon.transitionName = paths[position].file.absolutePath
                }
            } else {
                holder.icon.transitionName = paths[position].file.absolutePath
            }

            if (isSelectionMode) {
                holder.checkBox.visibility = View.VISIBLE
            } else {
                holder.checkBox.visibility = View.GONE
            }

            holder.checkBox.setChecked(paths[position].isSelected, false)
            holder.icon.loadAPKIcon(paths[position].file)
            holder.name.text = paths[position].file.absolutePath.substring(paths[position].file.absolutePath.lastIndexOf("/") + 1)
            holder.path.text = paths[position].file.absolutePath
            holder.info.text = paths[position].file.absolutePath.toSize() + " | " +
                    paths[position].file.absolutePath.substring(paths[position].file.absolutePath.lastIndexOf(".") + 1).uppercase(Locale.getDefault()) + " | " +
                    paths[position].file.lastModified().toDate()
            holder.name.setHiddenIcon(holder.name.text.startsWith("."))

            holder.container.setOnClickListener { view ->
                if (isSelectionMode) {
                    paths[position].isSelected = !paths[position].isSelected
                    holder.checkBox.setChecked(paths[position].isSelected, true)
                    isSelectionMode = paths.any { it.isSelected }
                    adapterCallbacks.onSelectionChanged()
                } else {
                    adapterCallbacks.onApkClicked(view, holder.bindingAdapterPosition.minus(1), holder.icon)
                }
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks.onApkLongClicked(it, holder.bindingAdapterPosition.minus(1), holder.icon)
                true
            }
        } else if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total), paths.size)

            holder.category.text = buildString {
                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK)) {
                    append(holder.getString(R.string.apk))
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS)) {
                    if (this.isNotEmpty()) {
                        append(" | ")
                    }

                    append(holder.getString(R.string.apks))
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM)) {
                    if (this.isNotEmpty()) {
                        append(" | ")
                    }

                    append(holder.getString(R.string.apkm))
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK)) {
                    if (this.isNotEmpty()) {
                        append(" | ")
                    }

                    append(holder.getString(R.string.xapk))
                }
            }

            holder.sorting.text = when (ApkBrowserPreferences.getSortStyle()) {
                SortApks.NAME -> {
                    holder.getString(R.string.name)
                }
                SortApks.SIZE -> {
                    holder.getString(R.string.size)
                }
                SortApks.DATE -> {
                    holder.getString(R.string.date)
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
            GlideApp.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return paths.size + 1
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
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val path: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.details)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val container: CondensedConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
        val loader: View = itemView.findViewById(R.id.loader)
    }

    fun loadSplitIcon() {
        for (i in 0 until paths.size) {
            if (paths[i].file.absolutePath.endsWith(".apkm") || paths[i].file.absolutePath.endsWith(".apks") || paths[i].file.absolutePath.endsWith(".zip")) {
                notifyItemChanged(i + 1)
            }
        }
    }
}
