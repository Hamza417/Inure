package app.simple.inure.ui.app

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterHomeMenu
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.app.AppsMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.popups.app.PopupHome
import app.simple.inure.terminal.Term
import app.simple.inure.ui.panels.*
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.HomeViewModel

class Home : ScopedFragment() {

    private lateinit var scrollView: NestedScrollView
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var navigationRecyclerView: RecyclerView
    private lateinit var appsCategoryRecyclerView: RecyclerView
    private lateinit var icon: ImageView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var options: DynamicRippleImageButton

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        scrollView = view.findViewById(R.id.home_scroll_view)
        header = view.findViewById(R.id.home_header)
        navigationRecyclerView = view.findViewById(R.id.home_menu)
        icon = view.findViewById(R.id.header_icon)
        appsCategoryRecyclerView = view.findViewById(R.id.apps_categories)
        search = view.findViewById(R.id.home_header_search_button)
        settings = view.findViewById(R.id.home_header_pref_button)
        options = view.findViewById(R.id.home_header_option_button)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.getMenuItems().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            navigationRecyclerView.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))

            val adapter = AdapterHomeMenu(it)

            adapter.setOnAppInfoMenuCallback(object : AdapterHomeMenu.AdapterHomeMenuCallbacks {
                override fun onMenuItemClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.apps) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Apps.newInstance(),
                                                        icon,
                                                        "apps")
                        }
                        getString(R.string.analytics) -> {
                            Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
                        }
                        getString(R.string.terminal) -> {
                            val intent = Intent(requireContext(), Term::class.java)
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), icon, icon.transitionName)
                            startActivity(intent, options.toBundle())
                        }
                        getString(R.string.usage_statistics) -> {
                            FragmentHelper.openFragment(
                                    requireActivity().supportFragmentManager,
                                    Statistics.newInstance(), icon, "stats")
                        }
                        getString(R.string.device_info) -> {
                            FragmentHelper.openFragment(
                                    requireActivity().supportFragmentManager,
                                    DeviceInformation.newInstance(), icon, "info")
                        }
                        getString(R.string.sensors) -> {
                            FragmentHelper.openFragment(
                                    requireActivity().supportFragmentManager,
                                    Sensors.newInstance(), icon, "sensors")
                        }
                        getString(R.string.batch) -> {
                            Toast.makeText(requireContext(), "This is a complicated feature and still in development. Likely to be available to test soon.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

            navigationRecyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        homeViewModel.getAppsCategory().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            appsCategoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))

            val adapterHomeMenu = AdapterHomeMenu(it)

            adapterHomeMenu.setOnAppInfoMenuCallback(object : AdapterHomeMenu.AdapterHomeMenuCallbacks {
                override fun onMenuItemClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.recently_installed) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        RecentlyInstalled.newInstance(), icon, "recently_installed")
                        }

                        getString(R.string.recently_updated) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        RecentlyUpdated.newInstance(), icon, "recently_updated")
                        }

                        getString(R.string.most_used) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        MostUsed.newInstance(), icon, "most_used")
                        }
                    }
                }
            })

            appsCategoryRecyclerView.adapter = adapterHomeMenu

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        search.setOnClickListener {
            clearTransitions()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "search")
        }

        settings.setOnClickListener {
            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                              MainPreferencesScreen.newInstance(),
                                              icon,
                                              "preferences_screen")
        }

        options.setOnClickListener {
            PopupHome(it).setOnPopupMenuCallback(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    when (source) {
                        getString(R.string.refresh) -> {
                            homeViewModel.refresh()
                        }
                        getString(R.string.preferences) -> {
                            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                                              MainPreferencesScreen.newInstance(),
                                                              icon,
                                                              "preferences_screen")
                        }
                    }
                }
            })
        }
    }

    private fun openAppInfo(packageInfo: PackageInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(packageInfo, icon.transitionName),
                                    icon, "app_info")
    }

    private fun openAppMenu(packageInfo: PackageInfo) {
        AppsMenu.newInstance(packageInfo)
            .show(childFragmentManager, "apps_menu")
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