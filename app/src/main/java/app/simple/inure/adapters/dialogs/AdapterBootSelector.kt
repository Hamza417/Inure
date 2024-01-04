package app.simple.inure.adapters.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterBootSelector(private val components: MutableList<Pair<String, Boolean>>) : RecyclerView.Adapter<AdapterBootSelector.Holder>() {

    private var bootSelectorCallbacks: BootSelectorCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_selector_split_apk, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.path.text = components.elementAt(position).first.subSequence(components.elementAt(position).first.lastIndexOf("/") + 1, components.elementAt(position).first.length)
        holder.checkBox.isChecked = components.elementAt(position).second

        holder.checkBox.setOnCheckedChangeListener { isChecked ->
            val newPair = Pair(components.elementAt(position).first, isChecked)
            components[position] = newPair
            components.elementAt(position).first.let { bootSelectorCallbacks?.onBootSelected(it, isChecked) }
        }

        holder.container.setOnClickListener {
            holder.checkBox.toggle()
        }
    }

    override fun getItemCount(): Int {
        return components.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.container)
    }

    fun setBootSelectorCallbacks(bootSelectorCallbacks: BootSelectorCallbacks) {
        this.bootSelectorCallbacks = bootSelectorCallbacks
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        notifyDataSetChanged()
    }

    companion object {
        interface BootSelectorCallbacks {
            fun onBootSelected(path: String, isChecked: Boolean)
        }
    }
}