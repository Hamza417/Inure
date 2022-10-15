package app.simple.inure.dialogs.batch

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.panels.BatchViewModelFactory
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.viewmodels.panels.BatchUninstallerViewModel

class BatchUninstaller : ScopedBottomSheetFragment() {

    private lateinit var count: TypeFaceTextView
    private lateinit var name: TypeFaceTextView
    private lateinit var progressStatus: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var percentage: TypeFaceTextView
    private lateinit var cancel: DynamicRippleTextView

    private var appList = arrayListOf<BatchPackageInfo>()

    private lateinit var batchUninstallerViewModel: BatchUninstallerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_uninstall, container, false)

        count = view.findViewById(R.id.progress_count)
        name = view.findViewById(R.id.name)
        progressStatus = view.findViewById(R.id.progress_status)
        progress = view.findViewById(R.id.progress)
        percentage = view.findViewById(R.id.progress_percentage)
        cancel = view.findViewById(R.id.cancel)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appList = requireArguments().getParcelableArrayList(BundleConstants.selectedBatchApps, BatchPackageInfo::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            appList = requireArguments().getParcelableArrayList(BundleConstants.selectedBatchApps)!!
        }

        if (ConfigurationPreferences.isUsingRoot()) {
            val batchViewModelFactory = BatchViewModelFactory(appList)
            batchUninstallerViewModel = ViewModelProvider(this, batchViewModelFactory)[BatchUninstallerViewModel::class.java]
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress.max = appList.size

        batchUninstallerViewModel.getDone().observe(viewLifecycleOwner) {
            progress.animateProgress(it, animate = true)
        }

        batchUninstallerViewModel.getFailed().observe(viewLifecycleOwner) {

        }

        batchUninstallerViewModel.getDone().observe(viewLifecycleOwner) {

        }
    }

    companion object {
        fun newInstance(list: ArrayList<BatchPackageInfo>): BatchUninstaller {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.selectedBatchApps, list)
            val fragment = BatchUninstaller()
            fragment.arguments = args
            return fragment
        }
    }
}