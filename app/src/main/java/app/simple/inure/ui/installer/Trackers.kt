package app.simple.inure.ui.installer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.viewmodels.installer.InstallerTrackersViewModel
import java.io.File

class Trackers : ScopedFragment() {

    private lateinit var message: TypeFaceTextView
    private lateinit var trackersViewModel: InstallerTrackersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_trackers, container, false)

        message = view.findViewById(R.id.message)

        val file: File? = requireArguments().serializable(BundleConstants.file)

        val installerViewModelFactory = InstallerViewModelFactory(null, file)
        trackersViewModel = ViewModelProvider(requireActivity(), installerViewModelFactory)[InstallerTrackersViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        trackersViewModel.getMessage().observe(viewLifecycleOwner) {
            Log.d("Trackers", "onViewCreated: ${it.first}")
            Log.d("Trackers", "onViewCreated: ${it.second}")
            message.text = it.first
        }
    }

    companion object {
        fun newInstance(file: File): Trackers {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Trackers()
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "Trackers"
    }
}