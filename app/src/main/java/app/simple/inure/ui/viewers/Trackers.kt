package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterTrackers
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.ui.subviewers.TrackerSourceViewer
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.TrackersViewModel

class Trackers : ScopedFragment() {

    private lateinit var progress: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var trackersViewModel: TrackersViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trackers, container, false)

        progress = view.findViewById(R.id.trackers_data_progress)
        recyclerView = view.findViewById(R.id.trackers_recycler_view)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        trackersViewModel = ViewModelProvider(this, packageInfoFactory)[TrackersViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        trackersViewModel.getClassesList().observe(viewLifecycleOwner) {
            progress.gone(true)
            val adapterTrackers = AdapterTrackers(it, "")

            adapterTrackers.setOnTrackersClickListener(object : AdapterTrackers.TrackersCallbacks {
                override fun onTrackersClicked(className: String) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                TrackerSourceViewer.newInstance(className, packageInfo),
                                                "tracker_source_viewer")
                }

                override fun onTrackersLongClicked(className: String) {
                    clearExitTransition()
                }
            })

            recyclerView.adapter = adapterTrackers
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Trackers {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Trackers()
            fragment.arguments = args
            return fragment
        }
    }
}