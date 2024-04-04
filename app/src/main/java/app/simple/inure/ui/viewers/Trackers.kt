package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterTrackers
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.trackers.TrackerSelector
import app.simple.inure.dialogs.trackers.TrackerSelector.Companion.showTrackerSelector
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.Tracker
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.TrackersPreferences
import app.simple.inure.ui.subviewers.TrackerInfo
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.TrackersViewModel

class Trackers : SearchBarScopedFragment() {

    private lateinit var checklist: DynamicRippleImageButton
    private lateinit var progress: CustomProgressBar
    private lateinit var ifwButton: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var trackersViewModel: TrackersViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trackers, container, false)

        checklist = view.findViewById(R.id.trackers_checklist)
        search = view.findViewById(R.id.trackers_search_btn)
        searchBox = view.findViewById(R.id.trackers_search)
        title = view.findViewById(R.id.trackers_title)
        progress = view.findViewById(R.id.trackers_data_progress)
        ifwButton = view.findViewById(R.id.trackers_ifw_btn)
        recyclerView = view.findViewById(R.id.trackers_recycler_view)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        trackersViewModel = ViewModelProvider(this, packageInfoFactory)[TrackersViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullVersionCheck()
        searchBoxState(animate = false, TrackersPreferences.isSearchVisible())
        startPostponedEnterTransition()

        trackersViewModel.getTrackers().observe(viewLifecycleOwner) { trackers ->
            setCount(trackers.size)
            progress.gone(true)
            if (trackers.isNotEmpty()) {
                search.visible(animate = true)
            } else {
                search.gone(true)
            }

            val adapterTrackers = AdapterTrackers(trackers, trackersViewModel.keyword)

            adapterTrackers.setOnTrackersClickListener(object : AdapterTrackers.TrackersCallbacks {
                override fun onTrackerSwitchChanged(tracker: Tracker, enabled: Boolean, position: Int) {
                    if (enabled) {
                        trackersViewModel.unblockTrackers(arrayListOf(tracker), position)
                    } else {
                        trackersViewModel.blockTrackers(arrayListOf(tracker), position)
                    }

                    trackersViewModel.getTracker().observe(viewLifecycleOwner) {
                        if (it != null) {
                            adapterTrackers.updateTracker(it)
                            trackersViewModel.clear()
                        }
                    }
                }

                override fun onTrackersClicked(tracker: Tracker) {
                    openFragmentSlide(TrackerInfo.newInstance(tracker), TrackerInfo.TAG)
                }
            })

            recyclerView.adapter = adapterTrackers

            checklist.setOnClickListener {
                childFragmentManager.showTrackerSelector(trackers, object : TrackerSelector.Companion.TrackerSelectorCallbacks {
                    override fun onEnableSelected(paths: ArrayList<Tracker>) {
                        progress.visible(animate = true)
                        trackersViewModel.unblockTrackers(paths)
                    }

                    override fun onDisableSelected(paths: ArrayList<Tracker>) {
                        progress.visible(animate = true)
                        trackersViewModel.blockTrackers(paths)
                    }
                })
            }

            if (ConfigurationPreferences.isUsingRoot()) {
                ifwButton.setOnClickListener {
                    openFragmentSlide(IFW.newInstance(packageInfo), "ifw_viewer")
                }

                if (trackers.isNotEmpty()) {
                    ifwButton.visible(animate = true)
                } else {
                    ifwButton.gone(false)
                }
            } else {
                ifwButton.gone(true)
            }

            if (trackers.size > 0) {
                if (ConfigurationPreferences.isUsingRoot()) {
                    checklist.visible(animate = true)
                }
            }

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    trackersViewModel.keyword = text.toString().trim()
                }
            }
        }

        trackersViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        trackersViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                TrackersPreferences.setSearchVisibility(!TrackersPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TrackersPreferences.trackersSearch -> {
                searchBoxState(animate = true, TrackersPreferences.isSearchVisible())
            }
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

        const val TAG = "Trackers"
    }
}
