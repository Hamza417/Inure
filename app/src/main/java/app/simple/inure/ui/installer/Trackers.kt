package app.simple.inure.ui.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterTrackers
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.interfaces.fragments.InstallerCallbacks
import app.simple.inure.models.Tracker
import app.simple.inure.ui.subviewers.TrackerInfo
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.viewmodels.installer.InstallerTrackersViewModel
import java.io.File

class Trackers : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var installerTrackersViewModel: InstallerTrackersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_trackers, container, false)

        recyclerView = view.findViewById(R.id.trackers_recycler_view)
        val file: File? = requireArguments().serializable(BundleConstants.file)

        val installerViewModelFactory = InstallerViewModelFactory(null, file)
        installerTrackersViewModel = ViewModelProvider(this, installerViewModelFactory)[InstallerTrackersViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        (parentFragment as InstallerCallbacks).onLoadingStarted()

        installerTrackersViewModel.getTrackers().observe(viewLifecycleOwner) { trackers ->
            (parentFragment as InstallerCallbacks).onLoadingFinished()
            val adapterTrackers = AdapterTrackers(trackers, "")

            adapterTrackers.setOnTrackersClickListener(object : AdapterTrackers.TrackersCallbacks {
                override fun onTrackerSwitchChanged(tracker: Tracker, enabled: Boolean, position: Int) {
                    if (enabled) {
                        installerTrackersViewModel.unblockTrackers(arrayListOf(tracker), position)
                    } else {
                        installerTrackersViewModel.blockTrackers(arrayListOf(tracker), position)
                    }

                    installerTrackersViewModel.getTracker().observe(viewLifecycleOwner) {
                        if (it != null) {
                            adapterTrackers.updateTracker(it)
                            installerTrackersViewModel.clearTrackersList()
                        }
                    }
                }

                override fun onTrackersClicked(tracker: Tracker) {
                    (parentFragment as ScopedFragment).clearTransitions()
                    openFragmentSlide(TrackerInfo.newInstance(tracker), TrackerInfo.TAG)
                }
            })

            recyclerView.adapter = adapterTrackers
        }

        installerTrackersViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }
    }

    override fun setupBackPressedDispatcher() {
        /* no-op */
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
