package app.simple.inure.adapters.viewers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.SharedLibraryModel
import app.simple.inure.util.FileSizeHelper.toSize

class AdapterSharedLibs(private val list: MutableList<SharedLibraryModel>) : RecyclerView.Adapter<AdapterSharedLibs.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_information, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position].name
        holder.data.text = list[position].size.toSize()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.information_heading)
        val data: TypeFaceTextView = itemView.findViewById(R.id.information_data)
    }
}
