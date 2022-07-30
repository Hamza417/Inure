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
import app.simple.inure.factories.actions.ExtractViewModelFactory
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.viewmodels.dialogs.ExtractViewModel

class Extract : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var progress: TypeFaceTextView
    private lateinit var status: TypeFaceTextView

    private lateinit var extractViewModel: ExtractViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_extract, container, false)

        loader = view.findViewById(R.id.extract_loader_indicator)
        progress = view.findViewById(R.id.extracting_progress)
        status = view.findViewById(R.id.extracting_updates)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        val extractViewModelFactory = ExtractViewModelFactory(packageInfo)
        extractViewModel = ViewModelProvider(this, extractViewModelFactory)[ExtractViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extractViewModel.getProgress().observe(viewLifecycleOwner) {
            progress.text = getString(R.string.progress, it)
        }

        extractViewModel.getStatus().observe(viewLifecycleOwner) {
            status.text = it
        }

        extractViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
            dismiss()
        }

        extractViewModel.getSuccess().observe(viewLifecycleOwner) {
            if (it) {
                status.text = getString(R.string.done)
                progress.invisible(true)
                loader.loaded()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Extract {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Extract()
            fragment.arguments = args
            return fragment
        }
    }
}