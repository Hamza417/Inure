package app.simple.inure.adapters.details

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.CheckBox
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.AppOpsModel

class AdapterOperations(private val ops: ArrayList<AppOpsModel>, private val packageInfo: PackageInfo) : RecyclerView.Adapter<AdapterOperations.Holder>() {

    private var adapterOpsCallbacks: AdapterOpsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_operations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = ops[position].title
        holder.desc.text = ops[position].description
        holder.checkBox.setChecked(ops[position].isEnabled)

        holder.checkBox.setOnCheckedChangeListener {
            adapterOpsCallbacks?.onCheckedChanged(ops[position], position)
        }
    }

    override fun getItemCount(): Int {
        return ops.size
    }

    fun updateOperation(appOpsModel: AppOpsModel, position: Int) {
        ops[position] = appOpsModel
        notifyItemChanged(position)
    }

    fun setOnOpsCheckedChangeListener(adapterOpsCallbacks: AdapterOpsCallbacks) {
        this.adapterOpsCallbacks = adapterOpsCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_ops_name)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.adapter_ops_desc)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    companion object {
        interface AdapterOpsCallbacks {
            fun onCheckedChanged(appOpsModel: AppOpsModel, position: Int)
        }
    }
}