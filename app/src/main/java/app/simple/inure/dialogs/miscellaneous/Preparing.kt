package app.simple.inure.dialogs.miscellaneous

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.PreparingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLConnection

class Preparing : ScopedBottomSheetFragment() {

    private lateinit var loader: ImageView
    private lateinit var updates: TypeFaceTextView
    private val preparingViewModel: PreparingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_send_prepare, container, false)

        loader = view.findViewById(R.id.preparing_loader_indicator)
        updates = view.findViewById(R.id.preparing_updates)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.loader))

        preparingViewModel.prepareApplicationFiles(applicationInfo)

        preparingViewModel.getProgress().observe(viewLifecycleOwner, {
            postUpdate(it)
        })

        preparingViewModel.getFile().observe(viewLifecycleOwner, {
            if (it.isNotNull()) {
                ShareCompat.IntentBuilder.from(requireActivity())
                        .setStream(FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", it!!))
                        .setType(URLConnection.guessContentTypeFromName(it.name))
                        .startChooser()

                dismiss()
            } else {
                postUpdate(getString(R.string.error))
            }
        })

    }

    private fun postUpdate(update: String) {
        updates.text = update
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Preparing {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Preparing()
            fragment.arguments = args
            return fragment
        }
    }
}