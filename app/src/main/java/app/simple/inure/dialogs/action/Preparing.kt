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
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.dialogs.FilePreparingViewModel
import java.net.URLConnection

class Preparing : ScopedBottomSheetFragment() {

    private lateinit var loader: ImageView
    private lateinit var updates: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var filePreparingViewModel: FilePreparingViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_send_prepare, container, false)

        loader = view.findViewById(R.id.preparing_loader_indicator)
        updates = view.findViewById(R.id.preparing_updates)
        progress = view.findViewById(R.id.preparing_progress)
        packageInfo = requireArguments().getParcelable("application_info")!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        filePreparingViewModel = ViewModelProvider(this, packageInfoFactory).get(FilePreparingViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.loader))

        filePreparingViewModel.getStatus().observe(viewLifecycleOwner) {
            postUpdate(it)
        }

        filePreparingViewModel.getProgress().observe(viewLifecycleOwner) {
            progress.text = getString(R.string.progress, it)
        }

        filePreparingViewModel.getFile().observe(viewLifecycleOwner) {
            if (it.isNotNull()) {
                ShareCompat.IntentBuilder(requireActivity())
                    .setStream(FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", it!!))
                    .setType(URLConnection.guessContentTypeFromName(it.name))
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
        fun newInstance(applicationInfo: PackageInfo): Preparing {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Preparing()
            fragment.arguments = args
            return fragment
        }
    }
}