package app.simple.inure.adapters.details

import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView

class AdapterInformation(private val list: ArrayList<Pair<Int, Spannable>>) : RecyclerView.Adapter<AdapterInformation.Holder>() {

    private var adapterInformationCallbacks: AdapterInformationCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_information, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.heading.setText(list[position].first)
        holder.data.text = list[position].second

        holder.container.setOnClickListener {
            adapterInformationCallbacks?.onInformationClicked(it, list[position].second.toString())
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_information_container)
        val heading: TypeFaceTextView = itemView.findViewById(R.id.information_heading)
        val data: TypeFaceTextView = itemView.findViewById(R.id.information_data)
    }

    fun setOnAdapterInformationCallbacks(adapterInformationCallbacks: AdapterInformationCallbacks) {
        this.adapterInformationCallbacks = adapterInformationCallbacks
    }

    companion object {
        interface AdapterInformationCallbacks {
            fun onInformationClicked(view: View, string: String)
        }
    }
}
