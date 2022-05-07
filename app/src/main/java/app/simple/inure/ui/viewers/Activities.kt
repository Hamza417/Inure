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
import app.simple.inure.adapters.details.AdapterActivities
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.action.ActivityLauncher
import app.simple.inure.dialogs.action.ComponentState
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.dialogs.miscellaneous.IntentAction
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.popups.viewers.PopupActivitiesMenu
import app.simple.inure.preferences.ActivitiesPreferences
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.ActivitiesViewModel

class Activities : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner

    private lateinit var activitiesViewModel: ActivitiesViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private var adapterActivities: AdapterActivities? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        recyclerView = view.findViewById(R.id.activities_recycler_view)
        search = view.findViewById(R.id.activities_search_btn)
        searchBox = view.findViewById(R.id.activities_search)
        title = view.findViewById(R.id.activities_title)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        activitiesViewModel = ViewModelProvider(this, packageInfoFactory).get(ActivitiesViewModel::class.java)

        searchBoxState()
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activitiesViewModel.getActivities().observe(viewLifecycleOwner) { it ->
            adapterActivities = AdapterActivities(packageInfo, it, searchBox.text.toString().trim())
            recyclerView.adapter = adapterActivities

            adapterActivities?.setOnActivitiesCallbacks(object : AdapterActivities.Companion.ActivitiesCallbacks {
                override fun onActivityClicked(activityInfoModel: ActivityInfoModel, packageId: String) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ActivityInfo.newInstance(activityInfoModel, packageInfo),
                                                "activity_info")
                }

                override fun onActivityLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int) {
                    PopupActivitiesMenu(icon, isComponentEnabled).setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.force_launch) -> {
                                    ActivityLauncher.newInstance(packageInfo, packageId)
                                        .show(childFragmentManager, "activity_launcher")
                                }
                                getString(R.string.force_launch_with_action) -> {
                                    IntentAction.newInstance(packageInfo, packageId)
                                        .show(childFragmentManager, "intent_action")
                                }
                                getString(R.string.enable), getString(R.string.disable) -> {
                                    val p = ComponentState.newInstance(packageInfo, packageId, isComponentEnabled)
                                    p.setOnComponentStateChangeListener(object : ComponentState.Companion.ComponentStatusCallbacks {
                                        override fun onSuccess() {
                                            adapterActivities?.notifyItemChanged(position)
                                        }
                                    })
                                    p.show(childFragmentManager, "component_state")
                                }
                            }
                        }
                    })
                }

                override fun onLaunchClicked(packageName: String, name: String) {
                    kotlin.runCatching {
                        ActivityUtils.launchPackage(requireContext(), packageName, name)
                    }.getOrElse {
                        Error.newInstance(it.message ?: getString(R.string.unknown))
                            .show(childFragmentManager, "error_dialog")
                    }
                }
            })

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    activitiesViewModel.getActivitiesData(text.toString().trim())
                }
            }
        }

        activitiesViewModel.getError().observe(viewLifecycleOwner) {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ActivitiesPreferences.setSearchVisibility(!ActivitiesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (ActivitiesPreferences.isSearchVisible()) {
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
            ActivitiesPreferences.activitySearch -> {
                searchBoxState()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Activities {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Activities()
            fragment.arguments = args
            return fragment
        }
    }
}
