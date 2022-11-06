package app.simple.inure.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatch
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.viewmodels.panels.BatchViewModel

class BatchSelectedApps : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var adapterBatch: AdapterBatch
    private lateinit var batchViewModel: BatchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_selected_batch_apps, container, false)

        recyclerView = view.findViewById(R.id.batch_selected_apps_rv)
        batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        batchViewModel.getSelectedApps().observe(viewLifecycleOwner) {
            adapterBatch = AdapterBatch(it, headerEnabled = false)

            adapterBatch.setOnItemClickListener(object : AdapterCallbacks {
                override fun onBatchChanged(batchPackageInfo: BatchPackageInfo) {
                    batchViewModel.updateBatchItem(batchPackageInfo, update = true)
                }
            })

            recyclerView.adapter = adapterBatch
        }
    }

    companion object {
        fun newInstance(): BatchSelectedApps {
            val args = Bundle()
            val fragment = BatchSelectedApps()
            fragment.arguments = args
            return fragment
        }
    }
}