package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.models.Bloat
import app.simple.inure.util.RecyclerViewUtils

class AdapterDebloat(private val bloats: ArrayList<Bloat>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context).inflate(R.layout.adapter_header_debloat, parent, false))
            }
            RecyclerViewUtils.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_debloat, parent, false))
            }
            else -> {
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_debloat, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition.minus(1)
        when (holder) {
            is Holder -> {
                holder.name.text = bloats[pos].packageInfo.applicationInfo.name
                holder.packageName.text = bloats[pos].packageInfo.packageName
                holder.desc.text = bloats[pos].description
                holder.checkBox.isChecked = true
                holder.icon.loadAppIcon(bloats[pos].packageInfo.packageName, bloats[pos].packageInfo.applicationInfo.enabled)
            }
            is Header -> {

            }
        }
    }

    override fun getItemCount(): Int {
        return bloats.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewUtils.TYPE_HEADER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: AppIconImageView = itemView.findViewById(R.id.app_icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.package_id)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.description)
        val checkBox: CheckBox = itemView.findViewById(R.id.check_box)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView)
}