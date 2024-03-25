package app.simple.inure.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionDescription
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.AppOp
import app.simple.inure.util.AdapterUtils

class AdapterOperations(private val ops: ArrayList<AppOp>, val keyword: String) : RecyclerView.Adapter<AdapterOperations.Holder>() {

    private var adapterOpsCallbacks: AdapterOpsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_operations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = ops[position].permission
        holder.desc.text = holder.context.getPermissionDescription(ops[position].id)
        holder.checkBox.isChecked = ops[position].isEnabled

        AdapterUtils.searchHighlighter(holder.name, keyword)
        AdapterUtils.searchHighlighter(holder.desc, keyword)

        holder.container.setOnClickListener {
            holder.checkBox.toggle()
        }

        holder.checkBox.setOnCheckedChangeListener {
            adapterOpsCallbacks?.onCheckedChanged(ops[position], position)
        }
    }

    override fun getItemCount(): Int {
        return ops.size
    }

    fun updateOperation(appOp: AppOp, position: Int) {
        ops[position] = appOp
        notifyItemChanged(position)
    }

    fun setOnOpsCheckedChangeListener(adapterOpsCallbacks: AdapterOpsCallbacks) {
        this.adapterOpsCallbacks = adapterOpsCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_ops_name)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.adapter_ops_desc)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_ops_container)
    }

    companion object {
        interface AdapterOpsCallbacks {
            fun onCheckedChanged(appOp: AppOp, position: Int)
        }
    }
}
