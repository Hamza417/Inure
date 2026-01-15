package app.simple.inure.adapters.viewers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionDescription
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.enums.AppOpMode
import app.simple.inure.models.AppOp
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.appendFlag
import java.util.Locale

class AdapterOperations(private val ops: ArrayList<AppOp>, val keyword: String) : RecyclerView.Adapter<AdapterOperations.Holder>() {

    private var adapterOpsCallbacks: AdapterOpsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_operations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = ops[position].permission.sanitize()
        holder.desc.text = holder.context.getPermissionDescription(ops[position].id)

        AdapterUtils.searchHighlighter(holder.name, keyword)
        AdapterUtils.searchHighlighter(holder.desc, keyword)

        holder.strip.text = buildString {
            when (ops[position].mode) {
                AppOpMode.ALLOW -> {
                    appendFlag(holder.getString(R.string.allowed))
                }
                AppOpMode.IGNORE -> {
                    appendFlag(holder.getString(R.string.ignored))
                }
                AppOpMode.DENY -> {
                    appendFlag(holder.getString(R.string.denied))
                }
                AppOpMode.FOREGROUND -> {
                    appendFlag(holder.getString(R.string.foreground))
                }
                AppOpMode.ASK -> {
                    appendFlag(holder.getString(R.string.ask))
                }
                AppOpMode.DEFAULT -> {
                    appendFlag(holder.getString(R.string.default_))
                }

                else -> {
                    appendFlag(holder.getString(R.string.unknown))
                }
            }

            when {
                !ops[position].rejectTime.isNullOrEmpty() -> {
                    appendFlag(ops[position].rejectTime!!)
                }

                !ops[position].time.isNullOrEmpty() -> {
                    appendFlag(ops[position].time!!)
                }
            }

            if (ops[position].mode == AppOpMode.ALLOW && !ops[position].duration.isNullOrEmpty()) {
                appendFlag(ops[position].duration)
            }
        }


        holder.container.setOnClickListener {
            adapterOpsCallbacks?.onAppOpClicked(ops[position], position)
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

    private fun String.sanitize(): String {
        return this.replace("_", " ").lowercase().replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.ROOT)
            } else {
                it.toString()
            }
        }
    }

    class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.desc)
        val strip: TypeFaceTextView = itemView.findViewById(R.id.strip)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_ops_container)
    }

    companion object {
        interface AdapterOpsCallbacks {
            fun onAppOpClicked(appOp: AppOp, position: Int)
        }
    }
}
