package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.constants.DebloatSortConstants
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.enums.Removal
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.parsers.LinkCallbacks
import app.simple.inure.models.Bloat
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.sort.DebloatSort
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.util.TextViewUtils.makeLinksClickable

class AdapterDebloat(private val bloats: ArrayList<Bloat>, private val header: Boolean = true, private val keyword: String = "") : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterDebloatCallback: AdapterDebloatCallback? = null
    private var isLoading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_debloat, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_debloat, parent, false))
            }
            else -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_debloat, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        val pos = if (header) {
            holder.bindingAdapterPosition.minus(1)
        } else {
            holder.bindingAdapterPosition
        }

        when (holder) {
            is Holder -> {
                holder.name.text = bloats[pos].packageInfo.applicationInfo.name
                holder.name.setWarningIcon(isWarning(bloats[pos]))
                holder.packageName.text = bloats[pos].packageInfo.packageName
                holder.flags.setBloatFlags(bloats[pos])
                holder.checkBox.isChecked = bloats[pos].isSelected

                if (bloats[pos].shouldHighlightBloat()) {
                    holder.container.setBloatWarningBackground(bloats[pos])
                } else {
                    holder.container.setDefaultBackground(false)
                }

                holder.desc.makeLinksClickable(bloats[pos].description.trim(), LinkCallbacks { url, _ ->
                    url?.asUri()?.openInBrowser(holder.desc.context)
                })

                holder.icon.loadAppIcon(
                        bloats[pos].packageInfo.packageName,
                        bloats[pos].packageInfo.applicationInfo.enabled,
                        bloats[pos].packageInfo.applicationInfo.sourceDir.toFile())

                holder.checkBox.setOnCheckedChangeListener {
                    bloats[pos].isSelected = it
                    adapterDebloatCallback?.onBloatSelected(bloats[pos])
                    if (header) {
                        notifyItemChanged(0) // Header
                    }
                }

                holder.container.setOnClickListener {
                    holder.checkBox.toggle()
                }

                holder.container.setOnLongClickListener {
                    adapterDebloatCallback?.onBloatLongPressed(bloats[pos])
                    true
                }

                if (keyword.isNotEmpty()) {
                    AdapterUtils.searchHighlighter(holder.name, keyword)
                    AdapterUtils.searchHighlighter(holder.packageName, keyword)
                    AdapterUtils.searchHighlighter(holder.flags, keyword)
                    AdapterUtils.searchHighlighter(holder.desc, keyword)
                }
            }
            is Header -> {
                if (isLoading) {
                    holder.loader.visibility = View.VISIBLE
                } else {
                    holder.loader.visibility = View.GONE
                }

                holder.totalSelected.text = holder.totalSelected.context.getString(R.string.selected_apps, bloats.count { it.isSelected })
                holder.total.text = holder.total.context.getString(R.string.total_apps, bloats.size.toString())
                holder.uadSubtitle.setOnClickListener {
                    UAD_REPO_LINK.asUri().openInBrowser(holder.uadSubtitle.context)
                }

                holder.category.text = when (DebloatPreferences.getApplicationType()) {
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

                holder.sorting.text = when (DebloatPreferences.getSortBy()) {
                    DebloatSort.SORT_BY_NAME -> {
                        holder.getString(R.string.name)
                    }
                    DebloatSort.SORT_BY_PACKAGE_NAME -> {
                        holder.getString(R.string.package_name)
                    }
                    else -> {
                        holder.getString(R.string.unknown)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (header) {
            bloats.size.plus(1)
        } else {
            bloats.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (header && position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val flags: TypeFaceTextView = itemView.findViewById(R.id.flags)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.desc)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val totalSelected: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_selected)
        val uadSubtitle: TypeFaceTextView = itemView.findViewById(R.id.uad_subtitle)
        val category: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_category)
        val sorting: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_sorting)
        val loader: CustomProgressBar = itemView.findViewById(R.id.loader)
    }

    fun setAdapterDebloatCallback(adapterDebloatCallback: AdapterDebloatCallback) {
        this.adapterDebloatCallback = adapterDebloatCallback
    }

    fun isAnyItemSelected(): Boolean {
        bloats.forEach {
            if (it.isSelected) {
                return true
            }
        }

        return false
    }

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyItemChanged(0)
    }

    private fun TypeFaceTextView.setBloatFlags(bloat: Bloat) {
        text = buildString {
            // State
            if (bloat.packageInfo.isInstalled()) {
                if (bloat.packageInfo.applicationInfo.enabled) {
                    appendFlag(this@setBloatFlags.context.getString(R.string.enabled))
                } else {
                    appendFlag(this@setBloatFlags.context.getString(R.string.disabled))
                }
            } else {
                appendFlag(this@setBloatFlags.context.getString(R.string.uninstalled))
            }

            // List
            when (bloat.list.lowercase()) {
                "aosp" -> {
                    appendFlag(context.getString(R.string.aosp))
                }
                "carrier" -> {
                    appendFlag(context.getString(R.string.carrier))
                }
                "google" -> {
                    appendFlag(context.getString(R.string.google))
                }
                "misc" -> {
                    appendFlag(context.getString(R.string.miscellaneous))
                }
                "oem" -> {
                    appendFlag(context.getString(R.string.oem))
                }
                "pending" -> {
                    appendFlag(context.getString(R.string.pending))
                }
                "unlisted" -> {
                    appendFlag(context.getString(R.string.unlisted))
                }
            }

            // Removal
            when (bloat.removal.method) {
                Removal.ADVANCED.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.advanced))
                }
                Removal.EXPERT.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.expert))
                }
                Removal.RECOMMENDED.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.recommended))
                }
                Removal.UNLISTED.method -> {
                    appendFlag(context.getString(R.string.unlisted))
                }
                Removal.UNSAFE.method -> {
                    appendFlag(this@setBloatFlags.context.getString(R.string.unsafe))
                }
            }
        }
    }

    private fun Bloat.shouldHighlightBloat(): Boolean {
        return when (this.removal.method) {
            Removal.ADVANCED.method -> {
                DebloatPreferences.getAdvancedHighlight()
            }
            Removal.EXPERT.method -> {
                DebloatPreferences.getExpertHighlight()
            }
            Removal.RECOMMENDED.method -> {
                DebloatPreferences.getRecommendedHighlight()
            }
            Removal.UNLISTED.method -> {
                DebloatPreferences.getUnlistedHighlight()
            }
            Removal.UNSAFE.method -> {
                DebloatPreferences.getUnsafeHighlight()
            }
            else -> {
                false
            }
        }
    }

    private fun isWarning(bloat: Bloat): Boolean {
        return bloat.removal.method == Removal.UNSAFE.method || bloat.removal.method == Removal.UNLISTED.method
    }

    fun updateSelections(mode: Int) {
        when (mode) {
            DebloatSortConstants.RECOMMENDED -> {
                when {
                    bloats.filter { it.removal.method == Removal.RECOMMENDED.method }.any {
                        it.removal.method == Removal.RECOMMENDED.method && it.isSelected.invert()
                    } -> {
                        deselectAny()

                        for (i in bloats.indices) {
                            if (bloats[i].removal.method == Removal.RECOMMENDED.method) {
                                bloats[i].isSelected = true
                                notifyItemChanged(i.plus(1))
                            }
                        }
                    }
                    bloats.filter { it.removal.method == Removal.RECOMMENDED.method }.all {
                        it.removal.method == Removal.RECOMMENDED.method && it.isSelected
                    } -> {
                        for (i in bloats.indices) {
                            if (bloats[i].removal.method == Removal.RECOMMENDED.method) {
                                bloats[i].isSelected = false
                                notifyItemChanged(i.plus(1))
                            }
                        }
                    }
                }
            }
            DebloatSortConstants.EXPERT -> {
                when {
                    bloats.filter { it.removal.method == Removal.EXPERT.method }.any {
                        it.removal.method == Removal.EXPERT.method && it.isSelected.invert()
                    } -> {
                        deselectAny()

                        for (i in bloats.indices) {
                            if (bloats[i].removal.method == Removal.EXPERT.method) {
                                bloats[i].isSelected = true
                                notifyItemChanged(i.plus(1))
                            }
                        }
                    }
                    bloats.filter { it.removal.method == Removal.EXPERT.method }.all {
                        it.removal.method == Removal.EXPERT.method && it.isSelected
                    } -> {
                        for (i in bloats.indices) {
                            if (bloats[i].removal.method == Removal.EXPERT.method) {
                                bloats[i].isSelected = false
                                notifyItemChanged(i.plus(1))
                            }
                        }
                    }
                }
            }
            DebloatSortConstants.ALL_REMOVAL -> {
                when {
                    bloats.any { it.isSelected.invert() } -> {
                        for (i in bloats.indices) {
                            bloats[i].isSelected = true
                            notifyItemChanged(i.plus(1))
                        }
                    }
                    bloats.all { it.isSelected } -> {
                        for (i in bloats.indices) {
                            bloats[i].isSelected = false
                            notifyItemChanged(i.plus(1))
                        }
                    }
                }
            }
        }

        notifyItemChanged(0) // Header
    }

    private fun deselectAny() {
        for (i in bloats.indices) {
            bloats[i].isSelected = false
            notifyItemChanged(i.plus(1))
        }
    }

    companion object {

        const val UAD_REPO_LINK = "https://github.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation"

        interface AdapterDebloatCallback {
            fun onBloatSelected(bloat: Bloat)
            fun onBloatLongPressed(bloat: Bloat)
        }
    }
}
