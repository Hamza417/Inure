package app.simple.inure.adapters.sub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterVirusTotalNamesList(private val names: List<String>) : RecyclerView.Adapter<AdapterVirusTotalNamesList.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_virustotal_names_sub, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = names[position]
    }

    override fun getItemCount(): Int {
        return names.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
    }
}