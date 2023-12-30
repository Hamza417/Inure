package app.simple.inure.ui.panels

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterQuickApps
import app.simple.inure.adapters.ui.AdapterHome
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.edgeeffect.EdgeEffectNestedScrollView
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.views.GridRecyclerView
import app.simple.inure.dialogs.app.ChangesReminder
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.ui.Store
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.QuickAppsViewModel
import rikka.shizuku.Shizuku

class Home : ScopedFragment() {

    private lateinit var scrollView: EdgeEffectNestedScrollView
    private lateinit var navigationRecyclerView: GridRecyclerView
    private lateinit var quickAppsRecyclerView: CustomHorizontalRecyclerView

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var quickAppViewModel: QuickAppsViewModel

    private fun getHomeLayout(): Int {
        return R.layout.fragment_home
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getHomeLayout(), container, false)

        scrollView = view.findViewById(R.id.home_scroll_view)
        quickAppsRecyclerView = view.findViewById(R.id.quick_app_recycler_view)
        navigationRecyclerView = view.findViewById(R.id.home_menu)

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        quickAppViewModel = ViewModelProvider(requireActivity())[QuickAppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences.shouldShowChangeLogReminder()) {
            ChangesReminder.newInstance()
                .show(childFragmentManager, "changes_reminder")
        }

        homeViewModel.getMenuItems().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            when (HomePreferences.getMenuLayout()) {
                PopupMenuLayout.GRID -> {
                    val gridLayoutManager = GridLayoutManager(context, getInteger(R.integer.span_count))

                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (it[position].first.isZero() || it[position].first == 1) {
                                getInteger(R.integer.span_count)
                            } else {
                                1
                            }
                        }
                    }

                    navigationRecyclerView.layoutManager = gridLayoutManager
                }

                PopupMenuLayout.VERTICAL -> {
                    navigationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }

            val adapter = AdapterHome(it)

            adapter.setOnAppInfoMenuCallback(object : AdapterHome.AdapterHomeMenuCallbacks {
                override fun onMenuItemClicked(source: Int, icon: ImageView) {
                    when (source) {
                        R.string.apps -> {
                            openFragmentArc(Apps.newInstance(), icon, "apps")
                        }

                        R.string.analytics -> {
                            openFragmentArc(Analytics.newInstance(), icon, "analytics")
                        }

                        R.string.terminal -> {
                            val intent = Intent(requireActivity(), Term::class.java)
                            if (BehaviourPreferences.isArcAnimationOn()) {
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), icon, icon.transitionName)
                                startActivity(intent, options.toBundle())
                            } else {
                                startActivity(intent)
                            }
                        }

                        R.string.usage_statistics -> {
                            openFragmentArc(Statistics.newInstance(), icon, "stats")
                        }

                        R.string.device_info -> {
                            openFragmentArc(DeviceInfo.newInstance(), icon, "info")
                        }
                        //                        R.string.sensors -> {
                        //                            openFragmentArc(Sensors.newInstance(), icon, "sensors")
                        //                        }
                        R.string.batch -> {
                            openFragmentArc(Batch.newInstance(), icon, "batch")
                        }

                        R.string.notes -> {
                            openFragmentArc(Notes.newInstance(), icon, "notes")
                        }

                        R.string.tags -> {
                            openFragmentArc(Tags.newInstance(), icon, "tags")
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

                        R.string.foss -> {
                            openFragmentArc(FOSS.newInstance(), icon, "foss")
                        }

                        R.string.hidden -> {
                            openFragmentArc(Hidden.newInstance(), icon, "hidden")
                        }

                        R.string.crash_report -> {
                            openFragmentArc(StackTraces.newInstance(), icon, "stacktraces")
                        }

                        R.string.battery_optimization -> {
                            if (ConfigurationPreferences.isUsingRoot()) {
                                openFragmentArc(BatteryOptimization.newInstance(), icon, "battery_optimization")
                            } else
                                if (ConfigurationPreferences.isUsingShizuku()) {
                                    if (Shizuku.pingBinder()) {
                                        openFragmentArc(BatteryOptimization.newInstance(), icon, "battery_optimization")
                                    } else {
                                        showWarning(Warnings.getShizukuFailedWarning(), goBack = false)
                                    }
                                } else {
                                    openFragmentArc(BatteryOptimization.newInstance(), icon, "battery_optimization")
                                }
                        }

                        R.string.boot_manager -> {
                            openFragmentArc(BootManager.newInstance(), icon, "boot_manager")
                        }

                        R.string.saved_commands -> {
                            openFragmentArc(TerminalCommands.newInstance(), icon, "saved_commands")
                        }

                        R.string.APKs -> {
                            openFragmentArc(APKs.newInstance(), icon, "apks")
                        }

                        R.string.fdroid -> {
                            openFragmentArc(Store.newInstance(), icon, "fdroid")
                        }

                        // Header

                        R.string.app_name -> {
                            openWebPage(getString(R.string.credits))
                        }

                        R.string.search -> {
                            openFragmentArc(Search.newInstance(firstLaunch = true), icon, "search")
                        }

                        R.string.preferences -> {
                            openFragmentArc(Preferences.newInstance(), icon, "preferences")
                        }

                        R.string.purchase -> {
                            openFragmentSlide(Trial.newInstance(), "trial")
                        }
                    }
                }
            })

            navigationRecyclerView.adapter = adapter

            if (AccessibilityPreferences.isAnimationReduced().invert()) {
                navigationRecyclerView.scheduleLayoutAnimation()
            }

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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            HomePreferences.homeMenuLayout -> {
                homeViewModel.refreshMenuItems()
            }
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