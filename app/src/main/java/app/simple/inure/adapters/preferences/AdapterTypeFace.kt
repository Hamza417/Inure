package app.simple.inure.adapters.preferences

import android.content.res.Resources
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.interfaces.adapters.AdapterTypeFaceCallbacks
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterTypeFace : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterTypeFaceCallbacks: AdapterTypeFaceCallbacks
    private var list = TypeFace.list.apply {
        sortBy { it.typefaceName }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
            }
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_typeface_header, parent, false))
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
                    holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textPrimary))
                } else {
                    holder.icon.invisible(false)
                    holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textTertiary))
                }

                holder.container.setOnClickListener {
                    adapterTypeFaceCallbacks.onTypeFaceClicked(list[position].name)
                }
            }
            is Header -> {
                holder.total.text = holder.itemView.context.getString(R.string.total, list.size)
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

    fun setOnTypeFaceClickListener(adapterTypeFaceCallbacks: AdapterTypeFaceCallbacks) {
        this.adapterTypeFaceCallbacks = adapterTypeFaceCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ImageView = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}
