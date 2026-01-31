package app.simple.inure.adapters.viewers

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.apk.utils.PermissionUtils.getPermissionDescription
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.Button
import app.simple.inure.enums.AppOpMode
import app.simple.inure.enums.AppOpScope
import app.simple.inure.models.AppOp
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.util.StringUtils.ifEmptyOrNull
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.Locale

class AdapterOperations(private val ops: ArrayList<AppOp>, var keyword: String) : RecyclerView.Adapter<AdapterOperations.Holder>() {

    private var adapterOpsCallbacks: AdapterOpsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_operations, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = ops[position].permission.sanitize()
        holder.desc.text = holder.context.getPermissionDescription(ops[position].id.ifEmptyOrNull(ops[position].permission))

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
                    appendFlag(holder.getString(R.string.default_identifier))
                }

                else -> {
                    appendFlag(holder.getString(R.string.unknown))
                }
            }

            when (ops[position].scope) {
                AppOpScope.UID -> {
                    appendFlag(holder.getString(R.string.uid))
                }
                AppOpScope.PACKAGE -> {
                    appendFlag(holder.getString(R.string.application))
                }
            }

            //            when {
            //                !ops[position].rejectTime.isNullOrEmpty() -> {
            //                    appendFlag(ops[position].rejectTime!!)
            //                }
            //
            //                !ops[position].time.isNullOrEmpty() -> {
            //                    appendFlag(ops[position].time!!)
            //                }
            //            }
            //
            //            if (ops[position].mode == AppOpMode.ALLOW && !ops[position].duration.isNullOrEmpty()) {
            //                appendFlag(ops[position].duration)
            //            }

            when (ops[position].mode) {
                AppOpMode.ALLOW -> {
                    holder.allow.isChecked = true
                }
                AppOpMode.IGNORE -> {
                    holder.ignore.isChecked = true
                }
                AppOpMode.DENY -> {
                    holder.deny.isChecked = true
                }
                else -> {
                    holder.stateGroup.clearChecked()
                }
            }

            holder.stateGroup.clearOnButtonCheckedListeners()
            holder.stateGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                Log.d("AppOpMode", "onBindViewHolder: $checkedId | isChecked: $isChecked")
                if (isChecked) {
                    when (checkedId) {
                        R.id.allow -> {
                            ops[position].mode = AppOpMode.ALLOW
                        }
                        R.id.ignore -> {
                            ops[position].mode = AppOpMode.IGNORE
                        }
                        R.id.deny -> {
                            ops[position].mode = AppOpMode.DENY
                        }
                        else -> {
                            ops[position].mode = AppOpMode.DEFAULT
                        }
                    }

                    Log.d("AppOpMode", "onBindViewHolder: ${ops[position].mode}")
                    adapterOpsCallbacks?.onAppOpStateChanged(ops[position], position)
                }
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(it: ArrayList<AppOp>, str: String) {
        keyword = str
        ops.clear()
        ops.addAll(it)
        notifyDataSetChanged()
    }

    class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val desc: TypeFaceTextView = itemView.findViewById(R.id.desc)
        val strip: TypeFaceTextView = itemView.findViewById(R.id.strip)
        val stateGroup: MaterialButtonToggleGroup = itemView.findViewById(R.id.state_group)
        val allow: Button = itemView.findViewById(R.id.allow)
        val ignore: Button = itemView.findViewById(R.id.ignore)
        val deny: Button = itemView.findViewById(R.id.deny)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_ops_container)

        init {
            allow.setButtonCheckedColor(Colors.ALLOW_COLOR.toColorInt())
            deny.setButtonCheckedColor(Colors.DENY_COLOR.toColorInt())
        }
    }

    companion object {
        interface AdapterOpsCallbacks {
            fun onAppOpClicked(appOp: AppOp, position: Int)
            fun onAppOpStateChanged(newAppOp: AppOp, position: Int)
        }
    }
}
