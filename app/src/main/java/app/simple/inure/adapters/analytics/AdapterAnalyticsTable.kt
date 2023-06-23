package app.simple.inure.adapters.analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.Analytics

class AdapterAnalyticsTable(private val analytics: ArrayList<Analytics>) : RecyclerView.Adapter<AdapterAnalyticsTable.Holder>() {

    private val total: Int by lazy {
        analytics.sumOf {
            it.count
        }
    }

    var onAnalyticsClicked: ((Analytics) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_analytics_sdk_data, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.sdk.text = analytics[position].label
        holder.code.text = analytics[position].code.toString()
        holder.total.text = analytics[position].count.toString()
        holder.percentage.text = holder.itemView.context.getString(R.string.progress, (analytics[position].count * 100 / total))

        holder.container.setOnClickListener {
            onAnalyticsClicked?.invoke(analytics[position])
        }
    }

    override fun getItemCount(): Int {
        return analytics.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val code: TypeFaceTextView = itemView.findViewById(R.id.code)
        val sdk: TypeFaceTextView = itemView.findViewById(R.id.sdk)
        val total: TypeFaceTextView = itemView.findViewById(R.id.total)
        val percentage: TypeFaceTextView = itemView.findViewById(R.id.percentage)
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.container)
    }
}