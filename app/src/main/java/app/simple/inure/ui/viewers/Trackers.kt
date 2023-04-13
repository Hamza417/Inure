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
import app.simple.inure.dialogs.trackers.TrackersMenu
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.preferences.TrackersPreferences
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.TrackersViewModel

class Trackers : SearchBarScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var progress: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var trackersViewModel: TrackersViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trackers, container, false)

        options = view.findViewById(R.id.trackers_option_btn)
        search = view.findViewById(R.id.trackers_search_btn)
        searchBox = view.findViewById(R.id.trackers_search)
        title = view.findViewById(R.id.trackers_title)
        progress = view.findViewById(R.id.trackers_data_progress)
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

        trackersViewModel.getTrackers().observe(viewLifecycleOwner) { it ->
            progress.gone(true)
            val adapterTrackers = AdapterTrackers(it, trackersViewModel.keyword)

            adapterTrackers.setOnTrackersClickListener(object : AdapterTrackers.TrackersCallbacks {
                override fun onTrackersClicked(any: Any, enabled: Boolean, position: Int) {
                    trackersViewModel.getActivityInfo().observe(viewLifecycleOwner) {
                        if (it.isNotNull()) {
                            adapterTrackers.updateActivityInfo(it)
                        }
                    }

                    trackersViewModel.getServiceInfo().observe(viewLifecycleOwner) {
                        if (it.isNotNull()) {
                            adapterTrackers.updateServiceInfo(it)
                        }
                    }

                    runCatching {
                        trackersViewModel.updateTrackersStatus(any as ActivityInfoModel, enabled, position)
                    }.onFailure {
                        kotlin.runCatching {
                            trackersViewModel.updateTrackersStatus(any as ServiceInfoModel, enabled, position)
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }
                }
            })

            recyclerView.adapter = adapterTrackers

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
    }
}