package app.simple.inure.adapters.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleMaterialCardView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.interfaces.terminal.TerminalCommandCallbacks
import app.simple.inure.models.TerminalCommand
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.RecyclerViewUtils

class AdapterTerminalCommands(private val terminalCommands: ArrayList<TerminalCommand>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var terminalCommandCallbacks: TerminalCommandCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_terminal_commands, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_terminal_commands, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            holder.label.text = terminalCommands[position].label
            holder.command.text = "cmd: " + terminalCommands[position].command
            holder.timestamp.text = holder.context.getString(R.string.edited_on, terminalCommands[position].dateCreated.toDate())

            if (terminalCommands[position].arguments.isNullOrEmpty()) {
                holder.arguments.text = "args: ${holder.getString(R.string.unspecified).lowercase()}"
            } else {
                holder.arguments.text = "args: " + terminalCommands[position].arguments
            }

            if (terminalCommands[position].description.isNullOrEmpty()) {
                holder.description.setText(R.string.desc_not_available)
            } else {
                holder.description.text = terminalCommands[position].description
            }

            holder.container.setOnClickListener {
                terminalCommandCallbacks?.onCommandClicked(terminalCommands[holder.bindingAdapterPosition.minus(1)])
            }

            holder.container.setOnLongClickListener {
                terminalCommandCallbacks?.onCommandLongClicked(
                        terminalCommands[holder.bindingAdapterPosition.minus(1)], it, holder.bindingAdapterPosition.minus(1))
                true
            }
        } else if (holder is Header) {
            holder.total.text = terminalCommands.size.toString()
        }
    }

    override fun getItemCount(): Int {
        return terminalCommands.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    fun setOnItemClickListener(terminalCommandCallbacks: TerminalCommandCallbacks) {
        this.terminalCommandCallbacks = terminalCommandCallbacks
    }

    fun removeItem(position: Int) {
        terminalCommands.removeAt(position)
        notifyItemChanged(0)
        notifyItemRemoved(position.plus(1))
        notifyItemRangeChanged(0, terminalCommands.size)
    }

    fun updateItem(terminalCommand: TerminalCommand, position: Int) {
        terminalCommands.removeAt(position)
        terminalCommands.add(position, terminalCommand)
        notifyItemChanged(0)
        notifyItemChanged(position.plus(1))
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val label: TypeFaceTextView = itemView.findViewById(R.id.label)
        val command: TypeFaceTextView = itemView.findViewById(R.id.command)
        val arguments: TypeFaceTextView = itemView.findViewById(R.id.arguments)
        val timestamp: TypeFaceTextView = itemView.findViewById(R.id.timestamp)
        val description: TypeFaceTextView = itemView.findViewById(R.id.description)
        val container: DynamicRippleMaterialCardView = itemView.findViewById(R.id.container)

        init {
            (itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = false
        }
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.total)

        init {
            (itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }
    }
}