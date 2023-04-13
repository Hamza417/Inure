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
import app.simple.inure.util.ConditionUtils.invert

class AdapterSplitApkSelector(private val paths: MutableList<Pair<String, Boolean>>) : RecyclerView.Adapter<AdapterSplitApkSelector.Holder>() {

    private var onSplitApkSelectorListener: OnSplitApkSelectorListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_split_apk_selector, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.path.text = paths.elementAt(position).first.subSequence(paths.elementAt(position).first.lastIndexOf("/") + 1, paths.elementAt(position).first.length)

        holder.checkBox.setChecked(paths.elementAt(position).second)

        if (paths.elementAt(position).first.endsWith("base.apk").invert()) {
            holder.checkBox.setOnCheckedChangeListener { isChecked ->
                val newPair = Pair(paths.elementAt(position).first, isChecked)
                paths[position] = newPair
                onSplitApkSelectorListener?.onSplitApkSelected(paths.elementAt(position).first, isChecked)
            }

            holder.container.setOnClickListener {
                holder.checkBox.toggle()
            }
        } else {
            holder.checkBox.setChecked(true)
            holder.checkBox.isEnabled = false
            holder.container.alpha = 0.5f
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

    fun setOnSplitApkSelectorListener(onSplitApkSelectorListener: OnSplitApkSelectorListener) {
        this.onSplitApkSelectorListener = onSplitApkSelectorListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        notifyDataSetChanged()
    }

    companion object {
        interface OnSplitApkSelectorListener {
            fun onSplitApkSelected(path: String, isChecked: Boolean)
        }
    }
}