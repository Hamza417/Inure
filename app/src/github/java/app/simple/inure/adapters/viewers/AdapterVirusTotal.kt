package app.simple.inure.adapters.viewers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.sub.AdapterVirusTotalNamesList
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
                            .inflate(R.layout.adapter_virustotal_total_votes, parent, false))
            }
            ANALYSIS_RESULT -> {
                AnalysisResultHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_virustotal_analysis_result, parent, false))
            }
            ANALYSIS_STATS -> {
                AnalysisStatsHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_virustotal_analysis_stats, parent, false))
            }
            NAMES -> {
                NamesHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_virustotal_names, parent, false))
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        when (holder) {
            is GeneralInfoHolder -> {
                holder.firstSubmissionDate.text = virusTotalResponse.firstSubmissionDate.times(1000L).toDate()
                holder.lastSubmissionDate.text = virusTotalResponse.lastSubmissionDate.times(1000L).toDate()
                holder.meaningfulName.text = virusTotalResponse.meaningfulName
                holder.sha256.text = virusTotalResponse.sha256
                holder.sha1.text = virusTotalResponse.sha1
                holder.md5.text = virusTotalResponse.md5
                holder.timesSubmitted.text = virusTotalResponse.timesSubmitted.toString()
            }
            is TotalVotesHolder -> {
                holder.satisfied.text = virusTotalResponse.totalVotes.harmless.toString()
                holder.notSatisfied.text = virusTotalResponse.totalVotes.malicious.toString()
            }
            is AnalysisResultHolder -> {
                val count = virusTotalResponse.lastAnalysisStats.malicious +
                        virusTotalResponse.lastAnalysisStats.suspicious

                holder.result.text = buildString {
                    append(count)
                    append(" / ")
                    append(virusTotalResponse.lastAnalysisResults.size)
                }

                holder.verdict.text = holder.getString(
                        R.string.virustotal_verdict,
                        count,
                        virusTotalResponse.lastAnalysisResults.size - count
                )
            }
            is AnalysisStatsHolder -> {
                // Already set in the holder constructor
            }
            is NamesHolder -> {
                holder.names.adapter = AdapterVirusTotalNamesList(virusTotalResponse.names)
            }
            else -> {
                throw IllegalArgumentException("Invalid view holder")
            }
        }
    }

    override fun getItemCount(): Int {
        return TOTAL_CARDS
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> GENERAL_INFO
            1 -> TOTAL_VOTES
            2 -> ANALYSIS_RESULT
            3 -> ANALYSIS_STATS
            4 -> NAMES
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

    inner class AnalysisResultHolder(itemView: View) : VerticalListViewHolder(itemView) {
        val result: TypeFaceTextView = itemView.findViewById(R.id.result)
        val verdict: TypeFaceTextView = itemView.findViewById(R.id.verdict)
    }

    inner class AnalysisStatsHolder(itemView: View) : VerticalListViewHolder(itemView) {
        private val malicious: TypeFaceTextView = itemView.findViewById(R.id.malicious)
        private val suspicious: TypeFaceTextView = itemView.findViewById(R.id.suspicious)
        private val undetected: TypeFaceTextView = itemView.findViewById(R.id.undetected)
        private val harmless: TypeFaceTextView = itemView.findViewById(R.id.harmless)
        private val timeout: TypeFaceTextView = itemView.findViewById(R.id.timeout)
        private val confirmedTimeout: TypeFaceTextView = itemView.findViewById(R.id.confirmed_timeout)
        private val failure: TypeFaceTextView = itemView.findViewById(R.id.failure)
        private val typeUnsupported: TypeFaceTextView = itemView.findViewById(R.id.type_unsupported)

        init {
            malicious.text = virusTotalResponse.lastAnalysisStats.malicious.toString()
            suspicious.text = virusTotalResponse.lastAnalysisStats.suspicious.toString()
            undetected.text = virusTotalResponse.lastAnalysisStats.undetected.toString()
            harmless.text = virusTotalResponse.lastAnalysisStats.harmless.toString()
            timeout.text = virusTotalResponse.lastAnalysisStats.timeout.toString()
            confirmedTimeout.text = virusTotalResponse.lastAnalysisStats.confirmedTimeout.toString()
            failure.text = virusTotalResponse.lastAnalysisStats.failure.toString()
            typeUnsupported.text = virusTotalResponse.lastAnalysisStats.typeUnsupported.toString()
        }
    }

    inner class NamesHolder(itemView: View) : VerticalListViewHolder(itemView) {
        val names: RecyclerView = itemView.findViewById(R.id.names_recycler_view)

        init {
            names.layoutManager = LinearLayoutManager(itemView.context)
            names.setHasFixedSize(true)
        }
    }

    companion object {
        private const val GENERAL_INFO = 0
        private const val TOTAL_VOTES = 1
        private const val ANALYSIS_RESULT = 2
        private const val ANALYSIS_STATS = 3
        private const val NAMES = 4

        private const val TOTAL_CARDS = 5
    }
}