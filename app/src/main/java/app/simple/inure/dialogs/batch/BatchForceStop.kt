package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.batch.BatchAppsFactory
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.util.ParcelUtils.parcelableArrayList
import app.simple.inure.viewmodels.dialogs.BatchForceStopViewModel

class BatchForceStop : ScopedBottomSheetFragment() {

    private lateinit var results: TypeFaceTextView

    private lateinit var viewModel: BatchForceStopViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_result, container, false)

        results = view.findViewById(R.id.result)
        val factory = BatchAppsFactory(requireArguments().parcelableArrayList(BundleConstants.selectedBatchApps)!!)
        viewModel = ViewModelProvider(this, factory).get(BatchForceStopViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getResults().observe(viewLifecycleOwner) {
            results.text = it
        }

        viewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it, dismiss = true)
        }
    }

    companion object {
        fun newInstance(packageInfo: ArrayList<BatchPackageInfo>): BatchForceStop {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.selectedBatchApps, packageInfo)
            val fragment = BatchForceStop()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchForceStop(packageInfo: ArrayList<BatchPackageInfo>): BatchForceStop {
            val dialog = newInstance(packageInfo)
            dialog.show(this, "batch_force_stop")
            return dialog
        }
    }
}