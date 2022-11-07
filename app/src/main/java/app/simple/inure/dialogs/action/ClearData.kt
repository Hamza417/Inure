package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.ClearDataViewModelFactory
import app.simple.inure.viewmodels.dialogs.ClearDataViewModel

class ClearData : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clear_data, container, false)

        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.clear_data_result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(ViewModelProvider(this, ClearDataViewModelFactory(packageInfo))[ClearDataViewModel::class.java]) {
            getResults().observe(viewLifecycleOwner) {

            }

            getSuccessStatus().observe(viewLifecycleOwner) {
                when (it) {
                    "Done" -> {
                        loader.loaded()
                        status.setText(R.string.cleared)
                    }
                    "Failed" -> {
                        loader.error()
                        status.setText(R.string.failed)
                    }
                }
            }

            getError().observe(viewLifecycleOwner) {
                showError(it)
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): ClearData {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = ClearData()
            fragment.arguments = args
            return fragment
        }
    }
}