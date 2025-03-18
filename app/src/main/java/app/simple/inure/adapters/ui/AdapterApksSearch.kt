package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAPKIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.ApkFile
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FileSizeHelper.toSize
import com.bumptech.glide.Glide
import java.util.Locale

class AdapterApksSearch(var paths: ArrayList<ApkFile> = arrayListOf(),
                        private val keyword: String,
                        private val transitionName: String,
                        private val transitionPosition: Int) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterCallbacks: AdapterCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return Holder(LayoutInflater.from(parent.context)
                          .inflate(R.layout.adapter_all_apps_small_details, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
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

            holder.icon.loadAPKIcon(paths[position].file)
            holder.name.text = paths[position].file.absolutePath.substring(paths[position].file.absolutePath.lastIndexOf("/") + 1)
            holder.path.text = paths[position].file.absolutePath
            holder.info.text = paths[position].file.absolutePath.toSize() + " | " +
                    paths[position].file.absolutePath.substring(paths[position].file.absolutePath.lastIndexOf(".") + 1).uppercase(Locale.getDefault()) + " | " +
                    paths[position].file.lastModified().toDate()

            holder.container.setOnClickListener {
                adapterCallbacks.onApkClicked(it, holder.bindingAdapterPosition, holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks.onApkLongClicked(it, holder.bindingAdapterPosition, holder.icon)
                true
            }

            AdapterUtils.searchHighlighter(holder.name, keyword)
            AdapterUtils.searchHighlighter(holder.path, keyword)
            AdapterUtils.searchHighlighter(holder.info, keyword)
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            Glide.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnItemClickListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val path: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val info: TypeFaceTextView = itemView.findViewById(R.id.details)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    fun loadSplitIcon() {
        for (i in 0 until paths.size) {
            if (paths[i].file.absolutePath.endsWith(".apkm") || paths[i].file.absolutePath.endsWith(".apks") || paths[i].file.absolutePath.endsWith(".zip")) {
                notifyItemChanged(i + 1)
            }
        }
    }
}
