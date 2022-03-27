package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatch
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.viewmodels.panels.BatchViewModel

class DialogBatchSelectedApps : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var adapterBatch: AdapterBatch
    private lateinit var batchViewModel: BatchViewModel
    private var batchSelectedAppsCallbacks: BatchSelectedAppsCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_selected_batch_apps, container, false)

        recyclerView = view.findViewById(R.id.batch_selected_apps_rv)
        batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("UNCHECKED_CAST") // Cast already checked
        adapterBatch = AdapterBatch(requireArguments().getSerializable(BundleConstants.selectedBatchApps) as ArrayList<BatchPackageInfo>, false)

        adapterBatch.setOnItemClickListener(object : AppsAdapterCallbacks {
            override fun onBatchChanged(batchPackageInfo: BatchPackageInfo) {
                batchViewModel.updateBatchItem(batchPackageInfo)
                batchSelectedAppsCallbacks?.onBatchChanged(batchPackageInfo)
            }
        })

        recyclerView.adapter = adapterBatch
    }

    fun setOnBatchSelectedAppsCallbacks(batchSelectedAppsCallbacks: BatchSelectedAppsCallbacks) {
        this.batchSelectedAppsCallbacks = batchSelectedAppsCallbacks
    }

    companion object {
        fun newInstance(currentAppsList: ArrayList<BatchPackageInfo>): DialogBatchSelectedApps {
            val args = Bundle()
            args.putSerializable(BundleConstants.selectedBatchApps, currentAppsList)
            val fragment = DialogBatchSelectedApps()
            fragment.arguments = args
            return fragment
        }

        interface BatchSelectedAppsCallbacks {
            fun onBatchChanged(batchPackageInfo: BatchPackageInfo)
        }
    }
}