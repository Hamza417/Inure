package app.simple.inure.adapters.terminal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class AdapterBackButtonAction : RecyclerView.Adapter<VerticalListViewHolder>() {

    private val list = arrayListOf(0, 1, 2, 3, 4)
    private var lastPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_type_face, parent, false))
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
                holder.textView.text = holder.itemView.context.getActionString(position)

                if (TerminalPreferences.getBackButtonAction() == position) {
                    holder.icon.visible(false)
                    lastPosition = holder.absoluteAdapterPosition
                } else {
                    holder.icon.invisible(false)
                }

                holder.container.setOnClickListener {
                    if (TerminalPreferences.setBackButtonAction(position)) {
                        notifyItemChanged(holder.absoluteAdapterPosition)
                        notifyItemChanged(lastPosition)
                    }
                }
            }
            is Header -> {
                holder.title.text = holder.itemView.context.getString(R.string.title_backaction_preference)
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
        } else RecyclerViewUtils.TYPE_ITEM
    }

    private fun Context.getActionString(position: Int): String {
        return when (position) {
            0 -> getString(R.string.closes_all_terminal_windows)
            1 -> getString(R.string.closes_current_terminal_window_only)
            2 -> getString(R.string.closes_activity_leaving_sessions_running)
            3 -> getString(R.string.sends_esc_to_terminal)
            4 -> getString(R.string.sends_tab_to_terminal)
            else -> throw IllegalArgumentException("Unknown Action")
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val textView: TypeFaceTextView = itemView.findViewById(R.id.adapter_typeface_textview)
        val icon: ThemeIcon = itemView.findViewById(R.id.adapter_typeface_check_icon)
        val container: LinearLayout = itemView.findViewById(R.id.adapter_typeface_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_header_title)
        val total: TextView = itemView.findViewById(R.id.adapter_type_face_total)
    }
}