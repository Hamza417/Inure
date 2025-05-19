package app.simple.inure.adapters.viewers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.virustotal.VirusTotalResponse

class AdapterVirusTotal(private val virusTotalResponse: VirusTotalResponse) : RecyclerView.Adapter<VerticalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            GENERAL_INFO -> {
                GeneralInfoHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_virustotal_info, parent, false))
            }
            TOTAL_VOTES -> {
                TotalVotesHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_total_votes, parent, false))
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        when {
            holder is GeneralInfoHolder -> {
                holder.firstSubmissionDate.text = virusTotalResponse.firstSubmissionDate.times(1000L).toDate()
                holder.lastSubmissionDate.text = virusTotalResponse.lastSubmissionDate.times(1000L).toDate()
                holder.meaningfulName.text = virusTotalResponse.meaningfulName
                holder.sha256.text = virusTotalResponse.sha256
                holder.sha1.text = virusTotalResponse.sha1
                holder.md5.text = virusTotalResponse.md5
                holder.timesSubmitted.text = virusTotalResponse.timesSubmitted.toString()
            }
            holder is TotalVotesHolder -> {
                holder.satisfied.text = virusTotalResponse.totalVotes.harmless.toString()
                holder.notSatisfied.text = virusTotalResponse.totalVotes.malicious.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> GENERAL_INFO
            1 -> TOTAL_VOTES
            else -> -1
        }
    }

    inner class GeneralInfoHolder(itemView: View) : VerticalListViewHolder(itemView) {
        val firstSubmissionDate: TypeFaceTextView = itemView.findViewById(R.id.first_submission_date)
        val lastSubmissionDate: TypeFaceTextView = itemView.findViewById(R.id.last_submission_date)
        val meaningfulName: TypeFaceTextView = itemView.findViewById(R.id.meaningful_name)
        val sha256: TypeFaceTextView = itemView.findViewById(R.id.sha256)
        val sha1: TypeFaceTextView = itemView.findViewById(R.id.sha1)
        val md5: TypeFaceTextView = itemView.findViewById(R.id.md5)
        val timesSubmitted: TypeFaceTextView = itemView.findViewById(R.id.times_submitted)

        init {
            val params = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }

    inner class TotalVotesHolder(itemView: View) : VerticalListViewHolder(itemView) {
        val satisfied: TypeFaceTextView = itemView.findViewById(R.id.satisfied_votes)
        val notSatisfied: TypeFaceTextView = itemView.findViewById(R.id.not_satisfied_votes)
    }

    companion object {
        private const val GENERAL_INFO = 0
        private const val TOTAL_VOTES = 1
    }
}