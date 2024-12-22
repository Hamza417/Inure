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
import app.simple.inure.adapters.viewers.AdapterActivities
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.action.ActivityLauncher
import app.simple.inure.dialogs.action.ComponentState
import app.simple.inure.dialogs.action.ComponentState.Companion.showComponentStateDialog
import app.simple.inure.dialogs.miscellaneous.IntentAction
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.popups.viewers.PopupActivitiesMenu
import app.simple.inure.preferences.ActivitiesPreferences
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.ActivityUtils
import app.simple.inure.viewmodels.viewers.ActivitiesViewModel

class Activities : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var activitiesViewModel: ActivitiesViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private var adapterActivities: AdapterActivities? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        recyclerView = view.findViewById(R.id.activities_recycler_view)
        search = view.findViewById(R.id.activities_search_btn)
        searchBox = view.findViewById(R.id.activities_search)
        title = view.findViewById(R.id.activities_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        activitiesViewModel = ViewModelProvider(this, packageInfoFactory)[ActivitiesViewModel::class.java]

        searchBoxState(false, ActivitiesPreferences.isSearchVisible())
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()

        activitiesViewModel.getActivities().observe(viewLifecycleOwner) { it ->
            adapterActivities = AdapterActivities(packageInfo, it, searchBox.text.toString().trim())
            setCount(it.size)

            adapterActivities?.setOnActivitiesCallbacks(object : AdapterActivities.Companion.ActivitiesCallbacks {
                override fun onActivityClicked(activityInfoModel: ActivityInfoModel, packageId: String) {
                    openFragmentSlide(ActivityInfo.newInstance(activityInfoModel, packageInfo), ActivityInfo.TAG)
                }

                override fun onActivityLongPressed(activityInfoModel: ActivityInfoModel, packageInfo: PackageInfo, icon: View, position: Int) {
                    val isEnabled = ActivityUtils.isEnabled(requireContext(), packageInfo.packageName, activityInfoModel.name)
                    PopupActivitiesMenu(requireView(), isEnabled).setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.force_launch) -> {
                                    ActivityLauncher.newInstance(packageInfo, activityInfoModel.name)
                                        .show(childFragmentManager, ActivityLauncher.TAG)
                                }

                                getString(R.string.force_launch_with_action) -> {
                                    IntentAction.newInstance(packageInfo, activityInfoModel.name)
                                        .show(childFragmentManager, IntentAction.TAG)
                                }
                                getString(R.string.enable), getString(R.string.disable) -> {
                                    showComponentStateDialog(packageInfo, activityInfoModel.name, isEnabled, object : ComponentState.Companion.ComponentStatusCallbacks {
                                        override fun onSuccess() {
                                            adapterActivities?.notifyItemChanged(position)
                                        }
                                    })
                                }
                                getString(R.string.create_shortcut) -> {
                                    if (activityInfoModel.exported) {
                                        ActivityUtils.createShortcut(requireContext(), activityInfoModel = activityInfoModel)
                                    } else {
                                        showWarning("ERR: " + getString(R.string.not_exported), false)
                                    }
                                }
                            }
                        }
                    })
                }

                override fun onLaunchClicked(packageName: String, name: String) {
                    kotlin.runCatching {
                        ActivityUtils.launchPackage(requireContext(), packageName, name)
                    }.getOrElse {
                        showError(it)
                    }
                }
            })

            recyclerView.setExclusiveAdapter(adapterActivities)
        }

        activitiesViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        activitiesViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_activities_found)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                activitiesViewModel.getActivitiesData(text.toString().trim())
            }
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ActivitiesPreferences.setSearchVisibility(!ActivitiesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ActivitiesPreferences.ACTIVITY_SEARCH -> {
                searchBoxState(true, ActivitiesPreferences.isSearchVisible())
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, keywords: String? = null): Activities {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.keywords, keywords)
            val fragment = Activities()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "activities"
    }
}
