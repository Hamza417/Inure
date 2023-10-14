package app.simple.inure.adapters.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class AdapterPathPicker : RecyclerView.Adapter<AdapterPathPicker.Holder>() {

    var paths = ArrayList<String>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onPathSelected: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_path_picker, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.directoryName.text = paths[position]

        holder.directoryName.setOnClickListener {
            onPathSelected?.invoke(paths[position])
        }
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val directoryName: DynamicRippleTextView = itemView.findViewById(R.id.directory_name)
    }

    companion object {
        private const val TAG = "PathPicker"
    }
}