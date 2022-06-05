package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.UnpackModel

class AdapterUnpack(val arrayList: ArrayList<UnpackModel>) : RecyclerView.Adapter<AdapterUnpack.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_unpack, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = arrayList[position].name
        holder.path.text = arrayList[position].path

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_unpack_name)
        val path: TypeFaceTextView = itemView.findViewById(R.id.adapter_unpack_path)
        val icon: ImageView = itemView.findViewById(R.id.adapter_unpack_icon)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_unpack_container)
    }
}