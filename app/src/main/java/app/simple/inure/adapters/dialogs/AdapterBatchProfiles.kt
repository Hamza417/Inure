package app.simple.inure.adapters.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.models.BatchProfile
import app.simple.inure.preferences.BatchPreferences

class AdapterBatchProfiles(private val names: ArrayList<BatchProfile>) : RecyclerView.Adapter<AdapterBatchProfiles.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_batch_profiles, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = names[position].profileName

        holder.name.setOnClickListener {
            BatchPreferences.setLastSelectedProfile(names[position].id)
        }
    }

    override fun getItemCount(): Int {
        return names.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: DynamicRippleTextView = itemView.findViewById(R.id.name)
    }
}