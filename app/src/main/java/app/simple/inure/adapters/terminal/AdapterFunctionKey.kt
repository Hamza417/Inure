package app.simple.inure.adapters.terminal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterFunctionKey : RecyclerView.Adapter<VerticalListViewHolder>() {

    private val list = Constants.getKeyList()

    var onError: (error: String) -> Unit = {}

    private var lastPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_special_keys, parent, false))
            }
            RecyclerViewUtils.TYPE_HEADER -> {
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
                holder.title.text = list[position]
                holder.code.text = buildString {
                    append("Keycode: ")
                    append(Constants.getKeyCode(list[position]).toString())
                }

                if (TerminalPreferences.getFnKey() == position) {
                    holder.icon.visible(false)
                    lastPosition = holder.absoluteAdapterPosition
                } else {
                    holder.icon.invisible(false)
                }

                if (TerminalPreferences.getControlKey() == position && position != list.size.minus(1)) {
                    holder.title.setTextColor("#e74c3c".toColorInt())
                }

                holder.container.setOnClickListener {
                    if (TerminalPreferences.getControlKey() == position && position != list.size.minus(1)) {
                        onError.invoke("${list[position]} is already assigned to CTRL key")
                    } else {
                        if (TerminalPreferences.setFnKey(position)) {
                            notifyItemChanged(holder.absoluteAdapterPosition)
                            notifyItemChanged(lastPosition)
                        }
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
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
        val title: TypeFaceTextView = itemView.findViewById(R.id.name)
        val code: TypeFaceTextView = itemView.findViewById(R.id.code)
        val icon: ThemeIcon = itemView.findViewById(R.id.check_icon)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}