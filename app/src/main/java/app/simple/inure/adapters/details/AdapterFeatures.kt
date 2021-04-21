package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import com.jaredrummler.apkparser.model.UseFeature

class AdapterFeatures(private val featuresList: List<UseFeature>) : RecyclerView.Adapter<AdapterFeatures.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_features, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = featuresList[position].name

        holder.required.text = if (featuresList[position].required) {
            holder.itemView.context.getString(R.string.required)
        } else {
            holder.itemView.context.getString(R.string.not_required)
        }
    }

    override fun getItemCount(): Int {
        return featuresList.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_features_name)
        val required: TypeFaceTextView = itemView.findViewById(R.id.adapter_features_required)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_features_container)
    }
}