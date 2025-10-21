package app.simple.inure.dialogs.batch

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.batch.AdapterBatchUninstaller
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.panels.BatchViewModelFactory
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.util.ParcelUtils.parcelableArrayList
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.dialogs.BatchUninstallerViewModel
import app.simple.inure.viewmodels.panels.BatchViewModel

class BatchUninstaller : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterBatchUninstaller: AdapterBatchUninstaller? = null

    private var appList = arrayListOf<BatchPackageInfo>()
    private var batchUninstallerViewModel: BatchUninstallerViewModel? = null
    private var batchViewModel: BatchViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_uninstall, container, false)

        loader = view.findViewById(R.id.loader)
        recyclerView = view.findViewById(R.id.recycler_view)

        appList = requireArguments().parcelableArrayList(BundleConstants.selectedBatchApps)!!
        batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]

        val batchViewModelFactory = BatchViewModelFactory(appList, requireArguments().getBoolean(BundleConstants.state))
        batchUninstallerViewModel = ViewModelProvider(this, batchViewModelFactory)[BatchUninstallerViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loader.start()

        batchUninstallerViewModel?.getData()?.observe(viewLifecycleOwner) {
            loader.gone()
            adapterBatchUninstaller = AdapterBatchUninstaller(it)
            recyclerView.adapter = adapterBatchUninstaller
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

        const val TAG = "BatchUninstaller"

        data class BatchUninstallerResult(
                val packageInfo: PackageInfo,
                val isSuccessful: Boolean
        )
    }
}
