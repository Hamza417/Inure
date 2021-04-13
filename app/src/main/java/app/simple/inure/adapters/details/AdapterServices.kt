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

class AdapterServices(private val services: List<AndroidComponent>) : RecyclerView.Adapter<AdapterServices.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_services, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = services[position].name.substring(services[position].name.lastIndexOf(".") + 1)
        holder.process.text = services[position].name
    }

    override fun getItemCount(): Int {
        return services.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_name)
        val process: TypeFaceTextView = itemView.findViewById(R.id.adapter_services_process)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_services_container)
    }
}