package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton

class AdapterFormattingStrip(private val list: List<Int>) : RecyclerView.Adapter<AdapterFormattingStrip.Holder>() {

    private var formattingStripCallbacks: FormattingStripCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_formatting_strip, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.button.setImageResource(list[position])

        holder.button.setOnClickListener {
            formattingStripCallbacks?.onFormattingButtonClicked(position.plus(1), it)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnFormattingStripCallbackListener(formattingStripCallbacks: FormattingStripCallbacks) {
        this.formattingStripCallbacks = formattingStripCallbacks
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val button: DynamicRippleImageButton = itemView.findViewById(R.id.button)
    }

    companion object {
        interface FormattingStripCallbacks {
            fun onFormattingButtonClicked(position: Int, view: View)
        }
    }
}