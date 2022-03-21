package app.simple.inure.adapters.preferences

import android.content.res.Resources
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterTypeFace : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var list = TypeFace.list.also { it ->
        it.subList(1, it.size).sortBy {
            it.typefaceName
        }
    }

    private var lastFontPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
            }
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_typeface, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        when (holder) {
            is Holder -> {
                try {
                    holder.textView.typeface = ResourcesCompat.getFont(holder.itemView.context, list[position].typeFaceResId)
                } catch (e: Resources.NotFoundException) {
                    holder.textView.typeface = Typeface.DEFAULT_BOLD
                }

                holder.textView.text = list[position].typefaceName

                if (AppearancePreferences.getAppFont() == list[position].name) {
                    holder.icon.visible(false)
                    holder.textView.setTextColor(ThemeManager.theme.textViewTheme.primaryTextColor)
                    lastFontPosition = holder.absoluteAdapterPosition
                } else {
                    holder.icon.invisible(false)
                    holder.textView.setTextColor(ThemeManager.theme.textViewTheme.tertiaryTextColor)
                }

                holder.container.setOnClickListener {
                    if (AppearancePreferences.setAppFont(list[position].name)) {
                        notifyItemChanged(lastFontPosition)
                        notifyItemChanged(holder.absoluteAdapterPosition)
                        notifyItemChanged(0) // Update the header font
                    }
                }
            }
            is Header -> {
                holder.total.text = holder.itemView.context.getString(R.string.total, list.size)

                // TODO - Find the exact cause of why the typeface is not changing
                holder.title.typeface = TypeFace.getTypeFace(AppearancePreferences.getAppFont(), TypeFace.TypefaceStyle.BOLD.style, holder.itemView.context)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position.isZero()) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val textView: TypeFaceTextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ImageView = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_type_face_total)
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
    }
}