package app.simple.inure.adapters.dialogs

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.NullSafety.isNotNullOrEmpty
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.virustotal.submodels.AnalysisResult

class AdapterVirusTotalAnalysisResult(private val data: HashMap<String, AnalysisResult>)
    : RecyclerView.Adapter<AdapterVirusTotalAnalysisResult.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_virustotal_results, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = data.entries.elementAt(position)
        holder.engine.text = item.key

        when (item.value.category) {
            AnalysisResult.CATEGORY_MALICIOUS, AnalysisResult.CATEGORY_SUSPICIOUS -> {
                holder.engine.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_close_12dp,
                        0,
                        0,
                        0
                )
                TextViewCompat.setCompoundDrawableTintList(holder.engine, ColorStateList.valueOf(FAILED))
            }
            AnalysisResult.CATEGORY_HARMLESS, AnalysisResult.CATEGORY_UNDETECTED -> {
                holder.engine.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_check_12dp,
                        0,
                        0,
                        0
                )
                TextViewCompat.setCompoundDrawableTintList(holder.engine, ColorStateList.valueOf(PASSED))
            }
            AnalysisResult.CATEGORY_FAILURE, AnalysisResult.CATEGORY_TIMEOUT, AnalysisResult.CATEGORY_CONFIRMED_TIMEOUT -> {
                holder.engine.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_warning_12dp,
                        0,
                        0,
                        0
                )
                TextViewCompat.setCompoundDrawableTintList(holder.engine, ColorStateList.valueOf(WARNING))
            }
            AnalysisResult.CATEGORY_TYPE_UNSUPPORTED -> {
                holder.engine.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_error_12dp,
                        0,
                        0,
                        0
                )
                TextViewCompat.setCompoundDrawableTintList(holder.engine, ColorStateList.valueOf(UNDETECTED))
            }
        }

        holder.data.text = buildString {
            if (item.value.engineVersion.isNotNullOrEmpty()) {
                appendFlag(item.value.engineVersion)
            }

            when (item.value.category) {
                AnalysisResult.CATEGORY_MALICIOUS -> appendFlag(holder.getString(R.string.malicious))
                AnalysisResult.CATEGORY_SUSPICIOUS -> appendFlag(holder.getString(R.string.suspicious))
                AnalysisResult.CATEGORY_HARMLESS -> appendFlag(holder.getString(R.string.harmless))
                AnalysisResult.CATEGORY_UNDETECTED -> appendFlag(holder.getString(R.string.undetected))
                AnalysisResult.CATEGORY_FAILURE -> appendFlag(holder.getString(R.string.failure))
                AnalysisResult.CATEGORY_TIMEOUT -> appendFlag(holder.getString(R.string.timeout))
                AnalysisResult.CATEGORY_CONFIRMED_TIMEOUT -> appendFlag(holder.getString(R.string.confirmed_timeout))
                AnalysisResult.CATEGORY_TYPE_UNSUPPORTED -> appendFlag(holder.getString(R.string.type_unsupported))
            }

            if (item.value.result.isNotNullOrEmpty()) {
                appendFlag(item.value.result)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val engine: TypeFaceTextView = itemView.findViewById(R.id.engine)
        val data: TypeFaceTextView = itemView.findViewById(R.id.data)

        init {
            data.setTypeface(data.typeface, Typeface.ITALIC)
        }
    }

    companion object {
        private const val PASSED = 0xFF16A085.toInt()
        private const val FAILED = 0xFFCB4335.toInt()
        private const val WARNING = 0xFFEB984E.toInt()
        private const val UNDETECTED = 0xFFCCD1D1.toInt()
    }
}