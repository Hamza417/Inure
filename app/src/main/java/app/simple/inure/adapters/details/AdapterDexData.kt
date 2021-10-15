package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import net.dongliu.apk.parser.bean.DexClass

class AdapterDexData(private val dexs: ArrayList<DexClass>) : RecyclerView.Adapter<AdapterDexData.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_dex_data, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.classType.text = dexs[position].classType
        holder.packageName.text = dexs[position].packageName
        holder.superClass.text = dexs[position].superClass

        var status = ""

        if (dexs[position].isPublic) {
            status = StringBuilder()
                    .append(status)
                    .append(holder.itemView.context.getString(R.string.public_identifier))
                    .toString()
        } else {
            status = StringBuilder()
                    .append(status)
                    .append(holder.itemView.context.getString(R.string.private_identifier))
                    .toString()
        }

        if (dexs[position].isProtected) {
            status = StringBuilder()
                    .append(status)
                    .append(" | ")
                    .append(holder.itemView.context.getString(R.string.protected_identifier))
                    .toString()
        }

        if (dexs[position].isStatic) {
            status = StringBuilder()
                    .append(status)
                    .append(" | ")
                    .append(holder.itemView.context.getString(R.string.static_identifier))
                    .toString()
        }

        if (dexs[position].isAnnotation) {
            status = StringBuilder()
                    .append(status)
                    .append(" | ")
                    .append(holder.itemView.context.getString(R.string.annotation_identifier))
                    .toString()
        }

        if (dexs[position].isInterface) {
            status = StringBuilder()
                    .append(status)
                    .append(" | ")
                    .append(holder.itemView.context.getString(R.string.interface_identifier))
                    .toString()
        }

        if (dexs[position].isEnum) {
            status = StringBuilder()
                    .append(status)
                    .append(" | ")
                    .append(holder.itemView.context.getString(R.string.enum_identifier))
                    .toString()
        }

        if (status.isEmpty()) {
            status = holder.itemView.context.getString(R.string.not_available)
        }

        holder.status.text = status
    }

    override fun getItemCount(): Int {
        return dexs.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val classType: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_class_type)
        val status: TypeFaceTextView = itemView.findViewById(R.id.adapter_dex_status)
        val superClass: TypeFaceTextView = itemView.findViewById(R.id.adapter_permissions_super_class)
        val packageName: TypeFaceTextView = itemView.findViewById(R.id.adapter_dex_package_name)
    }
}