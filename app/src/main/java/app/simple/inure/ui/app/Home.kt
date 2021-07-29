package app.simple.inure.ui.app

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterHomeRecentlyInstalled
import app.simple.inure.adapters.home.AdapterHomeRecentlyUpdated
import app.simple.inure.adapters.menus.AdapterHomeMenu
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomHorizontalRecyclerView
import app.simple.inure.dialogs.miscellaneous.Preparing
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupMainList
import app.simple.inure.ui.panels.Analytics
import app.simple.inure.ui.panels.Search
import app.simple.inure.ui.panels.Statistics
import app.simple.inure.ui.panels.Terminal
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.HomeViewModel

class Home : ScopedFragment() {

    private lateinit var navigationRecyclerView: RecyclerView
    private lateinit var recentlyInstalledRecyclerView: CustomHorizontalRecyclerView
    private lateinit var recentlyUpdatedRecyclerView: CustomHorizontalRecyclerView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        navigationRecyclerView = view.findViewById(R.id.home_menu)
        recentlyInstalledRecyclerView = view.findViewById(R.id.recently_installed_recycler_view)
        recentlyUpdatedRecyclerView = view.findViewById(R.id.recently_updated_recycler_view)
        search = view.findViewById(R.id.home_header_search_button)
        settings = view.findViewById(R.id.home_header_pref_button)

        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.getRecentApps().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            val adapter = AdapterHomeRecentlyInstalled(it)

            adapter.setOnRecentAppsClickedListener(object : AdapterHomeRecentlyInstalled.Companion.RecentlyAppsCallbacks {
                override fun onRecentAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                AppInfo.newInstance(packageInfo.applicationInfo, icon.transitionName),
                                                icon, "app_info")
                }

                override fun onRecentAppLongPressed(packageInfo: PackageInfo, icon: ImageView, anchor: ViewGroup) {
                    val popupMenu = PopupMainList(layoutInflater.inflate(R.layout.popup_main_list, PopupLinearLayout(requireContext()), true),
                                                  packageInfo.applicationInfo, icon, anchor)
                    popupMenu.setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String, applicationInfo: ApplicationInfo, icon: ImageView) {
                            when (source) {
                                getString(R.string.app_information) -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                AppInfo.newInstance(packageInfo.applicationInfo, icon.transitionName),
                                                                icon, "app_info")
                                }
                                getString(R.string.send) -> {
                                    Preparing.newInstance(packageInfo.applicationInfo)
                                            .show(parentFragmentManager, "send_app")
                                }
                            }
                        }
                    })
                }
            })

            recentlyInstalledRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        homeViewModel.getUpdatedApps().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            val adapter = AdapterHomeRecentlyUpdated(it)

            adapter.setOnRecentAppsClickedListener(object : AdapterHomeRecentlyUpdated.Companion.RecentlyUpdatedAppsCallbacks {
                override fun onRecentAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                AppInfo.newInstance(packageInfo.applicationInfo, icon.transitionName),
                                                icon, "app_info")
                }

                override fun onRecentAppLongPressed(packageInfo: PackageInfo, icon: ImageView, anchor: ViewGroup) {
                    val popupMenu = PopupMainList(layoutInflater.inflate(R.layout.popup_main_list, PopupLinearLayout(requireContext()), true),
                                                  packageInfo.applicationInfo, icon, anchor)
                    popupMenu.setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String, applicationInfo: ApplicationInfo, icon: ImageView) {
                            when (source) {
                                getString(R.string.app_information) -> {
                                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                                AppInfo.newInstance(packageInfo.applicationInfo, icon.transitionName),
                                                                icon, "app_info")
                                }
                                getString(R.string.send) -> {
                                    Preparing.newInstance(packageInfo.applicationInfo)
                                            .show(parentFragmentManager, "send_app")
                                }
                            }
                        }
                    })
                }
            })

            recentlyUpdatedRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        homeViewModel.getMenuItems().observe(viewLifecycleOwner, {

            postponeEnterTransition()

            navigationRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            navigationRecyclerView.setHasFixedSize(true)

            val adapter = AdapterHomeMenu(it)

            adapter.setOnAppInfoMenuCallback(object : AdapterHomeMenu.AdapterHomeMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.apps) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Apps.newInstance(),
                                                        icon,
                                                        "apps")
                        }
                        getString(R.string.analytics) -> {
                            FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Analytics.newInstance(), icon, "analytics")
                        }
                        getString(R.string.terminal) -> {
                            FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Terminal.newInstance(), icon, "terminal")
                        }
                        getString(R.string.usage_statistics) -> {
                            FragmentHelper.openFragment(
                                requireActivity().supportFragmentManager,
                                Statistics.newInstance(), icon, "stats")
                        }
                    }
                }
            })

            navigationRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        search.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "search")
        }

        settings.setOnClickListener {
            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                              MainPreferencesScreen.newInstance(),
                                              view.findViewById(R.id.imageView3),
                                              "preferences_screen")
        }
    }

    companion object {
        fun newInstance(): Home {
            val args = Bundle()
            val fragment = Home()
            fragment.arguments = args
            return fragment
        }
    }
}