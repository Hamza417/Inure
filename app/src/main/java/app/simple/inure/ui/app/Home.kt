package app.simple.inure.ui.app

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterQuickApps
import app.simple.inure.adapters.menus.AdapterHomeMenu
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.popups.app.PopupHome
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.ui.panels.*
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.QuickAppsViewModel

class Home : ScopedFragment() {

    private lateinit var scrollView: NestedScrollView
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var navigationRecyclerView: RecyclerView
    private lateinit var appsCategoryRecyclerView: RecyclerView
    private lateinit var quickAppsHeader: TypeFaceTextView
    private lateinit var quickAppsRecyclerView: CustomHorizontalRecyclerView
    private lateinit var icon: ImageView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var options: DynamicRippleImageButton

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var quickAppViewModel: QuickAppsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        scrollView = view.findViewById(R.id.home_scroll_view)
        appsCategoryRecyclerView = view.findViewById(R.id.apps_categories)
        quickAppsHeader = view.findViewById(R.id.quick_apps_tv)
        quickAppsRecyclerView = view.findViewById(R.id.quick_app_recycler_view)
        navigationRecyclerView = view.findViewById(R.id.home_menu)

        header = view.findViewById(R.id.home_header)
        icon = view.findViewById(R.id.header_icon)
        search = view.findViewById(R.id.home_header_search_button)
        settings = view.findViewById(R.id.home_header_pref_button)
        options = view.findViewById(R.id.home_header_option_button)

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        quickAppViewModel = ViewModelProvider(requireActivity())[QuickAppsViewModel::class.java]

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
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Analytics.newInstance(),
                                                        icon,
                                                        "analytics")
                        }
                        getString(R.string.terminal) -> {
                            if (TerminalPreferences.isUsingTermux() && PackageUtils.isPackageInstalledAndEnabled("com.termux", requirePackageManager())) {
                                PackageUtils.launchThisPackage(requireContext(), "com.termux")
                            } else {
                                val intent = Intent(requireActivity(), Term::class.java)
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), icon, icon.transitionName)
                                startActivity(intent, options.toBundle())
                            }
                        }
                        getString(R.string.usage_statistics) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Statistics.newInstance(), icon, "stats")
                        }
                        getString(R.string.device_info) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        DeviceInformation.newInstance(), icon, "info")
                        }
                        getString(R.string.sensors) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Sensors.newInstance(), icon, "sensors")
                        }
                        getString(R.string.batch) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Batch.newInstance(), icon, "batch")
                        }
                        getString(R.string.notes) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Notes.newInstance(), icon, "notes")
                        }
                    }
                }
            })

            navigationRecyclerView.adapter = adapter
            navigationRecyclerView.scheduleLayoutAnimation()

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
                        getString(R.string.uninstalled) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Uninstalled.newInstance(), icon, "uninstalled")
                        }
                        getString(R.string.disabled) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Disabled.newInstance(), icon, "disabled")
                        }
                    }
                }
            })

            appsCategoryRecyclerView.adapter = adapterHomeMenu

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        quickAppViewModel.getQuickApps().observe(viewLifecycleOwner) {
            if (it.size.isZero()) {
                quickAppsHeader.invisible(false)
                quickAppsRecyclerView.invisible(false)
            } else {
                quickAppsHeader.visible(false)
                quickAppsRecyclerView.visible(false)
            }

            val adapterQuickApps = AdapterQuickApps(it as ArrayList<PackageInfo>)

            adapterQuickApps.seyOnQuickAppAdapterCallbackListener(object : AdapterQuickApps.Companion.QuickAppsAdapterCallbacks {
                override fun onQuickAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onQuickAppLongClicked(packageInfo: PackageInfo, icon: ImageView, anchor: ViewGroup) {
                    openAppMenu(packageInfo)
                }
            })

            quickAppsRecyclerView.adapter = adapterQuickApps
        }

        search.setOnClickListener {
            clearTransitions()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "search")
        }

        settings.setOnClickListener {
            FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                              Preferences.newInstance(),
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
                                                              Preferences.newInstance(),
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