package app.simple.inure.dialogs.virustotal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.adapters.dialogs.AdapterVirusTotalAnalysisResult
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.virustotal.submodels.AnalysisResult

class VirusTotalAnalysisResult : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private val severityOrder = listOf(
            AnalysisResult.CATEGORY_MALICIOUS,
            AnalysisResult.CATEGORY_SUSPICIOUS,
            AnalysisResult.CATEGORY_HARMLESS,
            AnalysisResult.CATEGORY_UNDETECTED,
            AnalysisResult.CATEGORY_CONFIRMED_TIMEOUT,
            AnalysisResult.CATEGORY_TIMEOUT,
            AnalysisResult.CATEGORY_FAILURE,
            AnalysisResult.CATEGORY_TYPE_UNSUPPORTED
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_virustotal_analysis_results, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val results = (requireArguments().serializable<HashMap<String, AnalysisResult>>(BundleConstants.RESULT) ?: HashMap()).entries
            .sortedBy { entry ->
                severityOrder.indexOf(entry.value.category).let {
                    if (it == -1) Int.MAX_VALUE else it
                }
            }
            .associateTo(LinkedHashMap()) {
                it.toPair()
            }

        val adapter = AdapterVirusTotalAnalysisResult(results)
        recyclerView.adapter = adapter
    }

    companion object {
        private const val TAG = "AnalysisResult"

        fun newInstance(results: HashMap<String, AnalysisResult>): VirusTotalAnalysisResult {
            val args = Bundle()
            args.putSerializable(BundleConstants.RESULT, results)
            val fragment = VirusTotalAnalysisResult()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAnalysisResult(results: HashMap<String, AnalysisResult>): VirusTotalAnalysisResult {
            val fragment = newInstance(results)
            fragment.show(this, TAG)
            return fragment
        }
    }
}