package app.simple.inure.adapters.viewers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.condensed.CondensedDynamicRippleTextView
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.util.AdapterUtils

class AdapterDexData(private val dexs: ArrayList<String>, val keyword: String) : RecyclerView.Adapter<AdapterDexData.Holder>() {

    var onDetailsClicked: ((String) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_resources, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = dexs[position]
        AdapterUtils.searchHighlighter(holder.name, keyword, ignoreCasing = true)

        //        holder.packageName.text = dexs[position].packageName
        //        holder.superClass.text = dexs[position].superClass
        //
        //        var status = ""
        //
        //        status = if (dexs[position].isPublic) {
        //            StringBuilder()
        //                .append(status)
        //                .append(holder.itemView.context.getString(R.string.public_identifier))
        //                .toString()
        //        } else {
        //            StringBuilder()
        //                .append(status)
        //                .append(holder.itemView.context.getString(R.string.private_identifier))
        //                .toString()
        //        }
        //
        //        if (dexs[position].isProtected) {
        //            status = StringBuilder()
        //                .append(status)
        //                    .append(" | ")
        //                    .append(holder.itemView.context.getString(R.string.protected_identifier))
        //                    .toString()
        //        }
        //
        //        if (dexs[position].isStatic) {
        //            status = StringBuilder()
        //                    .append(status)
        //                    .append(" | ")
        //                    .append(holder.itemView.context.getString(R.string.static_identifier))
        //                    .toString()
        //        }
        //
        //        if (dexs[position].isAnnotation) {
        //            status = StringBuilder()
        //                    .append(status)
        //                    .append(" | ")
        //                    .append(holder.itemView.context.getString(R.string.annotation_identifier))
        //                    .toString()
        //        }
        //
        //        if (dexs[position].isInterface) {
        //            status = StringBuilder()
        //                    .append(status)
        //                    .append(" | ")
        //                    .append(holder.itemView.context.getString(R.string.interface_identifier))
        //                    .toString()
        //        }
        //
        //        if (dexs[position].isEnum) {
        //            status = StringBuilder()
        //                    .append(status)
        //                    .append(" | ")
        //                    .append(holder.itemView.context.getString(R.string.enum_identifier))
        //                    .toString()
        //        }
        //
        //        if (status.isEmpty()) {
        //            status = holder.itemView.context.getString(R.string.not_available)
        //        }
        //
        //        holder.status.text = status

        holder.name.setOnClickListener {
            onDetailsClicked?.invoke(dexs[position])
        }
    }

    override fun getItemCount(): Int {
        return dexs.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: CondensedDynamicRippleTextView = itemView.findViewById(R.id.adapter_resources_name)
    }
}
