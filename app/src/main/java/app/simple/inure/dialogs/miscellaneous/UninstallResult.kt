package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.PackageStateResult
import app.simple.inure.util.ParcelUtils.parcelableArrayList

class UninstallResult : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_uninstall_result, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireArguments().parcelableArrayList<PackageStateResult>(BundleConstants.result)
        recyclerView.adapter = app.simple.inure.adapters.dialogs.AdapterUninstallResult(data!!)
    }

    companion object {
        fun newInstance(data: ArrayList<PackageStateResult>): UninstallResult {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.result, data)
            val fragment = UninstallResult()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showUninstallResult(data: ArrayList<PackageStateResult>) {
            val dialog = newInstance(data)
            dialog.show(this, "uninstall_result")
        }
    }
}