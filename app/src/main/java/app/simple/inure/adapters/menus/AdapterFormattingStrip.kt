package app.simple.inure.adapters.menus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.HorizontalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton

class AdapterFormattingStrip(val icons: List<Int>) : RecyclerView.Adapter<AdapterFormattingStrip.Holder>() {

    private var formattingStripCallbacks: FormattingStripCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_formatting_strip, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.button.setImageResource(icons[position])

        holder.button.setOnClickListener {
            formattingStripCallbacks?.onFormattingStripClicked(icons[position], it)
        }
    }

    override fun getItemCount(): Int {
        return icons.size
    }

    inner class Holder(itemView: View) : HorizontalListViewHolder(itemView) {
        val button: DynamicRippleImageButton = itemView.findViewById(R.id.button)
    }

    fun setOnFormattingStripClickedListener(formattingStripCallbacks: FormattingStripCallbacks) {
        this.formattingStripCallbacks = formattingStripCallbacks
    }

    companion object {
        interface FormattingStripCallbacks {
            fun onFormattingStripClicked(id: Int, view: View)
        }
    }
}