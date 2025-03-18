package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleMaterialCardView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.util.AdapterUtils.setAppVisualStates
import app.simple.inure.util.DateUtils
import app.simple.inure.util.RecyclerViewUtils
import com.bumptech.glide.Glide

class AdapterNotes(var notes: ArrayList<NotesPackageInfo>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterCallbacks: AdapterCallbacks? = null

    var areNotesExpanded = NotesPreferences.areNotesExpanded()
        set(value) {
            field = value
            for (i in notes.indices) {
                notifyItemChanged(i.plus(1))
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_notes, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_notes, parent, false))
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
            // holder.icon.transitionName = notes[position].packageInfo.packageName

            holder.icon.transitionName = notes[position].packageInfo.packageName
            holder.icon.loadAppIcon(notes[position].packageInfo.packageName, notes[position].packageInfo.safeApplicationInfo.enabled)
            holder.name.text = notes[position].packageInfo.safeApplicationInfo.name
            holder.packageId.text = notes[position].packageInfo.packageName
            holder.note.text = notes[position].note.subSequence(0, notes[position].note.length.coerceAtMost(1000))

            holder.name.setAppVisualStates(notes[position].packageInfo)

            if (areNotesExpanded) {
                holder.note.maxLines = 60
            } else {
                holder.note.maxLines = 16
            }

            holder.updated.text = holder.context.getString(R.string.edited_on, DateUtils.formatDate(notes[position].dateUpdated))

            if (notes[position].packageInfo.safeApplicationInfo.enabled) {
                holder.name.paintFlags = holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            } else {
                holder.name.paintFlags = holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            holder.container.setOnClickListener {
                adapterCallbacks?.onNoteClicked(notes[position], holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onNoteLongClicked(
                        notes[holder.bindingAdapterPosition.minus(1)],
                        holder.bindingAdapterPosition.minus(1), it)
                true
            }
        }

        if (holder is Header) {
            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_notes), notes.size)
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            Glide.with(holder.icon).clear(holder.icon)
        }
        if (holder is Header) {
            Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
        }
    }

    override fun getItemCount(): Int {
        return notes.size + 1
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

    fun removeItem(position: Int) {
        notes.removeAt(position)
        notifyItemRemoved(position.plus(1))
        notifyItemChanged(0)
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.app_name)
        val packageId: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val updated: TypeFaceTextView = itemView.findViewById(R.id.date_updated)
        val note: TypeFaceTextView = itemView.findViewById(R.id.notes)
        val container: DynamicRippleMaterialCardView = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)

        init {
            val params = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }
}
