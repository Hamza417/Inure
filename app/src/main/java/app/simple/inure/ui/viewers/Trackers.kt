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
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.menus.TrackersMenu
import app.simple.inure.dialogs.trackers.TrackersMessage
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.preferences.TrackersPreferences
import app.simple.inure.ui.subviewers.TrackerSourceViewer
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.TrackersViewModel

class Trackers : ScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var progress: CustomProgressBar
    private lateinit var analytics: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var trackersViewModel: TrackersViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    private var message: Pair<String, String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trackers, container, false)

        options = view.findViewById(R.id.trackers_option_btn)
        search = view.findViewById(R.id.trackers_search_btn)
        analytics = view.findViewById(R.id.trackers_analytics_btn)
        searchBox = view.findViewById(R.id.trackers_search)
        title = view.findViewById(R.id.trackers_title)
        progress = view.findViewById(R.id.trackers_data_progress)
        recyclerView = view.findViewById(R.id.trackers_recycler_view)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        trackersViewModel = ViewModelProvider(this, packageInfoFactory)[TrackersViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBoxState()
        startPostponedEnterTransition()

        trackersViewModel.getClassesList().observe(viewLifecycleOwner) {
            progress.gone(true)
            val adapterTrackers = AdapterTrackers(it, trackersViewModel.keyword ?: "")

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

            analytics.setOnClickListener {
                if (message.isNotNull()) {
                    TrackersMessage.newInstance(message)
                        .show(childFragmentManager, "tracker_message")
                }
            }

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    trackersViewModel.keyword = text.toString().trim()
                }
            }
        }

        trackersViewModel.getMessage().observe(viewLifecycleOwner) {
            message = it

            if (TrackersPreferences.isMessageShownAutomatically()) {
                TrackersMessage.newInstance(message)
                    .show(childFragmentManager, "tracker_message")
            }
        }

        options.setOnClickListener {
            TrackersMenu.newInstance()
                .show(childFragmentManager, "trackers_menu")
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                TrackersPreferences.setSearchVisibility(!TrackersPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (TrackersPreferences.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(true)
            searchBox.showInput()
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(true)
            searchBox.gone()
            searchBox.hideInput()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TrackersPreferences.trackersSearch -> {
                searchBoxState()
            }
            TrackersPreferences.isTrackersFullList -> {
                trackersViewModel.organizeData()
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
    }
}