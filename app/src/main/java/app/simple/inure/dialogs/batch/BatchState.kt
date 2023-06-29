package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.panels.BatchViewModelFactory
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.viewmodels.dialogs.BatchStateViewModel

class BatchState : ScopedBottomSheetFragment() {

    private lateinit var state: TypeFaceTextView

    private var appList = arrayListOf<BatchPackageInfo>()
    private var batchStateViewModel: BatchStateViewModel? = null
    var onBatchStateUpdated: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_state, container, false)

        state = view.findViewById(R.id.state)

        appList = requireArguments().serializable(BundleConstants.selectedBatchApps)!!
        val batchViewModelFactory = BatchViewModelFactory(appList, requireArguments().getBoolean(BundleConstants.state))
        batchStateViewModel = ViewModelProvider(this, batchViewModelFactory)[BatchStateViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (app in appList) {
            Log.d("BatchState", "onViewCreated: ${app.packageInfo.packageName}")
        }

        state.text = buildString {
            append(getString(R.string.count_queued, appList.size))
        }

        batchStateViewModel?.getSuccess()?.observe(viewLifecycleOwner) {
            state.text = buildString {
                if (requireArguments().getBoolean(BundleConstants.state)) {
                    append(getString(R.string.n_enabled, it))
                } else {
                    append(getString(R.string.n_disabled, it))
                }
                append(" | ")
                append(getString(R.string.count_failed, appList.size - it))
            }

            onBatchStateUpdated?.invoke()
        }
    }

    companion object {
        fun newInstance(list: ArrayList<BatchPackageInfo>, state: Boolean): BatchState {
            val args = Bundle()
            args.putSerializable(BundleConstants.selectedBatchApps, list)
            args.putBoolean(BundleConstants.state, state)
            val fragment = BatchState()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchStateDialog(list: ArrayList<BatchPackageInfo>, state: Boolean, onBatchStateUpdated: (() -> Unit)? = null) {
            newInstance(list, state).apply {
                this.onBatchStateUpdated = onBatchStateUpdated
            }.show(this, "batch_state")
        }
    }
}