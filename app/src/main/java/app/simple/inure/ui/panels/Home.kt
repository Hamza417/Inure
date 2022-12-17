package app.simple.inure.ui.panels

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterQuickApps
import app.simple.inure.adapters.menus.AdapterHomeMenu
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalledAndEnabled
import app.simple.inure.decorations.edgeeffect.EdgeEffectNestedScrollView
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.app.FullVersionReminder.Companion.showFullVersionReminder
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.popups.app.PopupHome
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.ui.music.Music
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.QuickAppsViewModel

class Home : ScopedFragment() {

    private lateinit var scrollView: EdgeEffectNestedScrollView
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var navigationRecyclerView: RecyclerView
    private lateinit var quickAppsRecyclerView: CustomHorizontalRecyclerView
    private lateinit var icon: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var options: DynamicRippleImageButton
    private lateinit var purchase: DynamicRippleImageButton

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var quickAppViewModel: QuickAppsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        scrollView = view.findViewById(R.id.home_scroll_view)
        quickAppsRecyclerView = view.findViewById(R.id.quick_app_recycler_view)
        navigationRecyclerView = view.findViewById(R.id.home_menu)

        header = view.findViewById(R.id.home_header)
        icon = view.findViewById(R.id.header_icon)
        search = view.findViewById(R.id.home_header_search_button)
        settings = view.findViewById(R.id.home_header_pref_button)
        options = view.findViewById(R.id.home_header_option_button)
        purchase = view.findViewById(R.id.home_purchase)

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        quickAppViewModel = ViewModelProvider(requireActivity())[QuickAppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences.isFullVersion()) {
            purchase.gone(animate = false)
        }

        homeViewModel.getMenuItems().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            val gridLayoutManager = GridLayoutManager(context, getInteger(R.integer.span_count))

            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (it[position].first.isZero()) getInteger(R.integer.span_count) else 1
                }
            }

            navigationRecyclerView.layoutManager = gridLayoutManager

            val adapter = AdapterHomeMenu(it)

            adapter.setOnAppInfoMenuCallback(object : AdapterHomeMenu.AdapterHomeMenuCallbacks {
                override fun onMenuItemClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.apps -> {
                            openFragmentArc(Apps.newInstance(), icon, "apps")
                        }
                        R.string.analytics -> {
                            openFragmentArc(Analytics.newInstance(), icon, "analytics")
                        }
                        R.string.terminal -> {
                            if (TerminalPreferences.isUsingTermux() && requirePackageManager().isPackageInstalledAndEnabled("com.termux")) {
                                PackageUtils.launchThisPackage(requireContext(), "com.termux")
                            } else {
                                val intent = Intent(requireActivity(), Term::class.java)
                                if (BehaviourPreferences.isArcAnimationOn()) {
                                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), icon, icon.transitionName)
                                    startActivity(intent, options.toBundle())
                                } else {
                                    startActivity(intent)
                                }
                            }
                        }
                        R.string.usage_statistics -> {
                            openFragmentArc(Statistics.newInstance(), icon, "stats")
                        }
                        R.string.device_info -> {
                            openFragmentArc(DeviceInformation.newInstance(), icon, "info")
                        }
                        R.string.sensors -> {
                            openFragmentArc(Sensors.newInstance(), icon, "sensors")
                        }
                        R.string.batch -> {
                            openFragmentArc(Batch.newInstance(), icon, "batch")
                        }
                        R.string.notes -> {
                            openFragmentArc(Notes.newInstance(), icon, "notes")
                        }
                        R.string.music -> {
                            openFragmentArc(Music.newInstance(), icon, "music")
                        }
                        R.string.recently_installed -> {
                            openFragmentArc(RecentlyInstalled.newInstance(), icon, "recently_installed")
                        }

                        R.string.recently_updated -> {
                            openFragmentArc(RecentlyUpdated.newInstance(), icon, "recently_updated")
                        }

                        R.string.most_used -> {
                            openFragmentArc(MostUsed.newInstance(), icon, "most_used")
                        }
                        R.string.uninstalled -> {
                            openFragmentArc(Uninstalled.newInstance(), icon, "uninstalled")
                        }
                        R.string.disabled -> {
                            openFragmentArc(Disabled.newInstance(), icon, "disabled")
                        }
                        R.string.stacktraces -> {
                            openFragmentArc(StackTraces.newInstance(), icon, "stacktraces")
                        }
                        R.string.battery_optimization -> {
                            openFragmentArc(BatteryOptimization.newInstance(), icon, "battery_optimization")
                        }
                        R.string.boot_manager -> {
                            openFragmentArc(BootManager.newInstance(), icon, "boot_manager")
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

        quickAppViewModel.getQuickApps().observe(viewLifecycleOwner) {
            if (it.size.isZero()) {
                quickAppsRecyclerView.invisible(false)
            } else {
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

        icon.setOnClickListener {
            openWebPage(getString(R.string.credits))
        }

        icon.setOnLongClickListener {
            openWebPage(getString(R.string.change_logs))
            true
        }

        search.setOnClickListener {
            openFragmentSlide(Search.newInstance(true), "search")
        }

        settings.setOnClickListener {
            openFragmentLinear(Preferences.newInstance(), it, "preferences_screen")
        }

        options.setOnClickListener {
            PopupHome(it).setOnPopupMenuCallback(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: Int) {
                    when (source) {
                        R.string.refresh -> {
                            homeViewModel.refresh()
                        }
                        R.string.preferences -> {
                            openFragmentLinear(Preferences.newInstance(), icon, "preferences_screen")
                        }
                    }
                }
            })
        }

        purchase.setOnClickListener {
            childFragmentManager.showFullVersionReminder()
        }
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