package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView

class AdapterInformation(private val list: ArrayList<Pair<String, String>>) : RecyclerView.Adapter<AdapterInformation.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_information, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.heading.text = list[position].first
        holder.data.text = list[position].second
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val heading: TypeFaceTextView = itemView.findViewById(R.id.information_heading)
        val data: TypeFaceTextView = itemView.findViewById(R.id.information_data)
    }
}
