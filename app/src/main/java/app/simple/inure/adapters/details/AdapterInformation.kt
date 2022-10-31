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
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils

class AdapterInformation(private val list: ArrayList<Pair<Int, Spannable>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterInformationCallbacks: AdapterInformationCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_divider_preferences, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_information, parent, false))
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.heading.setText(list[position].first)
            holder.data.text = list[position].second

            holder.container.setOnClickListener {
                adapterInformationCallbacks?.onInformationClicked(it, list[position].second.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].first.isZero()) {
            RecyclerViewUtils.TYPE_DIVIDER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleLinearLayout = itemView.findViewById(R.id.adapter_information_container)
        val heading: TypeFaceTextView = itemView.findViewById(R.id.information_heading)
        val data: TypeFaceTextView = itemView.findViewById(R.id.information_data)
    }

    inner class Divider(itemView: View) : VerticalListViewHolder(itemView)

    fun setOnAdapterInformationCallbacks(adapterInformationCallbacks: AdapterInformationCallbacks) {
        this.adapterInformationCallbacks = adapterInformationCallbacks
    }

    companion object {
        interface AdapterInformationCallbacks {
            fun onInformationClicked(view: View, string: String)
        }
    }
}
