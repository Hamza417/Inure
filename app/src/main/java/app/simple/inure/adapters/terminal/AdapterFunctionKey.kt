package app.simple.inure.adapters.terminal

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterFunctionKey : RecyclerView.Adapter<VerticalListViewHolder>() {

    private val list = arrayListOf(
            "Jog Ball",
            "@ (Address Sign)",
            "Left Alt",
            "Right Alt",
            "Vol Up",
            "Vol Down",
            "Camera",
            "None",
    )

    var onError: (error: String) -> Unit = {}

    private var lastPosition = 0

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        when (holder) {
            is Holder -> {
                holder.textView.text = list[position]

                if (TerminalPreferences.getFnKey() == position) {
                    holder.icon.visible(false)
                    lastPosition = holder.absoluteAdapterPosition
                } else {
                    holder.icon.invisible(false)
                }

                if (TerminalPreferences.getControlKey() == position && position != list.size.minus(1)) {
                    holder.textView.setTextColor(Color.parseColor("#e74c3c"))
                }

                holder.container.setOnClickListener {
                    kotlin.runCatching {
                        if (TerminalPreferences.getControlKey() == position && position != list.size.minus(1)) {
                            throw IllegalArgumentException("${list[position]} is assigned to CTRL key")
                        } else {
                            if (TerminalPreferences.setFnKey(position)) {
                                notifyItemChanged(holder.absoluteAdapterPosition)
                                notifyItemChanged(lastPosition)
                            }
                        }
                    }.onFailure {
                        onError.invoke(it.stackTraceToString())
                    }
                }
            }
            is Header -> {
                holder.title.text = holder.itemView.context.getString(R.string.title_fnkey_preference)
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

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val textView: TypeFaceTextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ImageView = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}