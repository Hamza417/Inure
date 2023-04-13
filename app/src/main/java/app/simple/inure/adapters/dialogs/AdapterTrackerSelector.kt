package app.simple.inure.adapters.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterTrackerSelector(private val paths: MutableList<Pair<String, Boolean>>) : RecyclerView.Adapter<AdapterTrackerSelector.Holder>() {

    private var trackerSelectorCallbacks: TrackerSelectorCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_split_apk_selector, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.path.text = paths.elementAt(position).first.subSequence(paths.elementAt(position).first.lastIndexOf("/") + 1, paths.elementAt(position).first.length)

        holder.checkBox.setChecked(paths.elementAt(position).second)

        holder.checkBox.setOnCheckedChangeListener { isChecked ->
            val newPair = Pair(paths.elementAt(position).first, isChecked)
            paths[position] = newPair
            paths.elementAt(position).first.let { trackerSelectorCallbacks?.onTrackerSelected(it, isChecked) }
        }

        holder.container.setOnClickListener {
            holder.checkBox.toggle()
        }
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val path: TypeFaceTextView = itemView.findViewById(R.id.path)
        val checkBox: InureCheckBox = itemView.findViewById(R.id.checkbox)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.container)
    }

    fun setTrackerSelectorCallbacks(trackerSelectorCallbacks: TrackerSelectorCallbacks) {
        this.trackerSelectorCallbacks = trackerSelectorCallbacks
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        notifyDataSetChanged()
    }

    companion object {
        interface TrackerSelectorCallbacks {
            fun onTrackerSelected(path: String, isChecked: Boolean)
        }
    }
}