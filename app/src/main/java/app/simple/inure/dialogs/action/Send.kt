package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.ExtractViewModelFactory
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.dialogs.ExtractViewModel

class Send : ScopedBottomSheetFragment() {

    private lateinit var loader: ImageView
    private lateinit var updates: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var extractViewModel: ExtractViewModel
    private lateinit var extractViewModelFactory: ExtractViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_send_prepare, container, false)

        loader = view.findViewById(R.id.preparing_loader_indicator)
        updates = view.findViewById(R.id.preparing_updates)
        progress = view.findViewById(R.id.preparing_progress)

        val paths = mutableSetOf<String>()

        try {
            paths.add(packageInfo.safeApplicationInfo.publicSourceDir ?: packageInfo.applicationInfo?.sourceDir!!)
        } catch (_: NullPointerException) {
            showWarning(Warnings.INVALID_FILE_AND_APP_DATA)
        }

        kotlin.runCatching {
            paths.addAll(packageInfo.safeApplicationInfo.splitSourceDirs!!)
        }

        extractViewModelFactory = ExtractViewModelFactory(packageInfo, paths)
        extractViewModel = ViewModelProvider(this, extractViewModelFactory)[ExtractViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.loader))

        extractViewModel.getStatus().observe(viewLifecycleOwner) {
            postUpdate(it)
        }

        extractViewModel.getProgress().observe(viewLifecycleOwner) {
            progress.text = getString(R.string.progress, it)
        }

        extractViewModel.getFile().observe(viewLifecycleOwner) {
            if (it.isNotNull()) {
                ShareCompat.IntentBuilder(requireActivity())
                    .setStream(FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", it!!))
                    .setType("*/*")
                    .startChooser()

                dismiss()
            } else {
                postUpdate(getString(R.string.error))
            }
        }
    }

    private fun postUpdate(update: String) {
        updates.text = update
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Send {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Send()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showSend(packageInfo: PackageInfo): Send {
            val dialog = newInstance(packageInfo)
            dialog.show(this, TAG)
            return dialog
        }

        const val TAG = "send"
    }
}
