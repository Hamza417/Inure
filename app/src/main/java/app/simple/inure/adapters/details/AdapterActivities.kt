package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import com.jaredrummler.apkparser.model.AndroidComponent

class AdapterActivities(private val activities: List<AndroidComponent>) : RecyclerView.Adapter<AdapterActivities.Holder>() {

    private lateinit var activitiesCallbacks: ActivitiesCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_services, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = activities[position].name.substring(activities[position].name.lastIndexOf(".") + 1)
        holder.process.text = activities[position].name
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_name)
        val process: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_process)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_services_container)
    }

    fun setOnActivitiesCallbacks (activitiesCallbacks: ActivitiesCallbacks) {
        this.activitiesCallbacks = activitiesCallbacks
    }

    interface ActivitiesCallbacks {
        fun onActivityClicked(androidComponent: AndroidComponent)
    }
}