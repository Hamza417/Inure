package app.simple.inure.ui.app

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterAppsSimple
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.action.Preparing
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.app.PopupMainList
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.popups.apps.PopupSortingStyle
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.panels.Search
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.ui.viewers.Information
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.AppsViewModel

class Apps : ScopedFragment() {

    private lateinit var appsListRecyclerView: CustomVerticalRecyclerView
    private lateinit var adapter: AdapterAppsSimple
    private lateinit var model: AppsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)

        model = ViewModelProvider(requireActivity())[AppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.getAppData().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            adapter = AdapterAppsSimple()
            adapter.apps = it

            appsListRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapter.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPress(packageInfo: PackageInfo, anchor: View, icon: ImageView, position: Int) {
                    PopupMainList(anchor, packageInfo.packageName).setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.launch) -> {
                                    packageInfo.launchThisPackage(requireContext())
                                }
                                getString(R.string.app_information) -> {
                                    clearTransitions()
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                Information.newInstance(packageInfo),
                                                                "information")
                                }
                                getString(R.string.send) -> {
                                    Preparing.newInstance(packageInfo)
                                        .show(parentFragmentManager, "send_app")
                                }
                            }
                        }
                    })
                }

                override fun onSearchPressed(view: View) {
                    clearTransitions()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                Search.newInstance(true),
                                                "search")
                }

                override fun onFilterPressed(view: View) {
                    PopupAppsCategory(view)
                }

                override fun onSortPressed(view: View) {
                    PopupSortingStyle(view)
                }

                override fun onSettingsPressed(view: View) {
                    clearExitTransition()
                    FragmentHelper.openFragment(parentFragmentManager, MainPreferencesScreen.newInstance(), "prefs_screen")
                }
            })
        }

        model.appLoaded.observe(viewLifecycleOwner) { appsEvent ->
            appsEvent.getContentIfNotHandledOrReturnNull()?.let {
                Log.d("Apps", if (it) "Apps Loaded" else "Failed")
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun openAppInfo(packageInfo: PackageInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(packageInfo, icon.transitionName),
                                    icon, "app_info")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle,
            MainPreferences.isSortingReversed,
            MainPreferences.listAppsCategory,
            -> {
                model.loadAppData()
            }
        }
    }

    companion object {
        fun newInstance(): Apps {
            val args = Bundle()
            val fragment = Apps()
            fragment.arguments = args
            return fragment
        }
    }
}