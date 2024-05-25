package app.simple.inure.ui.panels

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterQuickApps
import app.simple.inure.adapters.ui.AdapterHome
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.edgeeffect.EdgeEffectNestedScrollView
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.views.GridRecyclerView
import app.simple.inure.dialogs.app.AppMenu.Companion.showAppMenu
import app.simple.inure.dialogs.app.ChangesReminder.Companion.showChangesReminder
import app.simple.inure.dialogs.home.HomeMenu.Companion.showHomeMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.util.AppUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.utils.GooglePlayUtils.showAppReview
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.QuickAppsViewModel
import rikka.shizuku.Shizuku

class Home : ScopedFragment() {

    private lateinit var scrollView: EdgeEffectNestedScrollView
    private lateinit var navigationRecyclerView: GridRecyclerView
    private lateinit var quickAppsRecyclerView: CustomHorizontalRecyclerView

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var quickAppViewModel: QuickAppsViewModel
    private lateinit var adapterHome: AdapterHome

    private var data: List<Pair<Int, Int>>? = null

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
            childFragmentManager.showChangesReminder()
        } else {
            MainPreferences.setChangeLogReminder(BuildConfig.VERSION_CODE)
        }

        showRateDialog()

        homeViewModel.getMenuItems().observe(viewLifecycleOwner) {
            postponeEnterTransition()
            data = it
            adapterHome = AdapterHome(it)
            setLayoutManager()

            adapterHome.setOnAppInfoMenuCallback(object : AdapterHome.AdapterHomeMenuCallbacks {
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
                            openFragmentArc(Statistics.newInstance(), icon, Statistics.TAG)
                        }

                        R.string.device_info -> {
                            openFragmentArc(DeviceInfo.newInstance(), icon, DeviceInfo.TAG)
                        }
                        //                        R.string.sensors -> {
                        //                            openFragmentArc(Sensors.newInstance(), icon, "sensors")
                        //                        }
                        R.string.batch -> {
                            openFragmentArc(Batch.newInstance(), icon, Batch.TAG)
                        }

                        R.string.notes -> {
                            openFragmentArc(Notes.newInstance(), icon, Notes.TAG)
                        }

                        R.string.tags -> {
                            openFragmentArc(Tags.newInstance(), icon, Tags.TAG)
                        }

                        R.string.music -> {
                            openFragmentArc(Music.newInstance(), icon, Music.TAG)
                        }

                        R.string.recently_installed -> {
                            openFragmentArc(RecentlyInstalled.newInstance(), icon, RecentlyInstalled.TAG)
                        }

                        R.string.recently_updated -> {
                            openFragmentArc(RecentlyUpdated.newInstance(), icon, RecentlyUpdated.TAG)
                        }

                        R.string.most_used -> {
                            openFragmentArc(MostUsed.newInstance(), icon, MostUsed.TAG)
                        }

                        R.string.uninstalled -> {
                            openFragmentArc(Uninstalled.newInstance(), icon, Uninstalled.TAG)
                        }

                        R.string.disabled -> {
                            openFragmentArc(Disabled.newInstance(), icon, Disabled.TAG)
                        }

                        R.string.foss -> {
                            openFragmentArc(FOSS.newInstance(), icon, FOSS.TAG)
                        }

                        R.string.debloat -> {
                            openFragmentArc(Debloat.newInstance(), icon, Debloat.TAG)
                        }

                        R.string.hidden -> {
                            openFragmentArc(Hidden.newInstance(), icon, Hidden.TAG)
                        }

                        R.string.crash_report -> {
                            openFragmentArc(StackTraces.newInstance(), icon, StackTraces.TAG)
                        }

                        R.string.battery_optimization -> {
                            if (ConfigurationPreferences.isUsingRoot()) {
                                openFragmentArc(BatteryOptimization.newInstance(), icon, BatteryOptimization.TAG)
                            } else
                                if (ConfigurationPreferences.isUsingShizuku()) {
                                    if (Shizuku.pingBinder()) {
                                        openFragmentArc(BatteryOptimization.newInstance(), icon, BatteryOptimization.TAG)
                                    } else {
                                        showWarning(Warnings.getShizukuFailedWarning(), goBack = false)
                                    }
                                } else {
                                    openFragmentArc(BatteryOptimization.newInstance(), icon, BatteryOptimization.TAG)
                                }
                        }

                        R.string.boot_manager -> {
                            openFragmentArc(BootManager.newInstance(), icon, BootManager.TAG)
                        }

                        R.string.terminal_commands -> {
                            openFragmentArc(TerminalCommands.newInstance(), icon, TerminalCommands.TAG)
                        }

                        R.string.APKs -> {
                            openFragmentArc(APKs.newInstance(), icon, APKs.TAG)
                        }

                        // Header

                        R.string.app_name -> {
                            openWebPage(getString(R.string.credits))
                        }

                        R.string.search -> {
                            openFragmentArc(Search.newInstance(firstLaunch = true), icon, Search.TAG)
                        }

                        R.string.preferences -> {
                            Log.d(TAG, "onMenuItemClicked: ${icon.transitionName}")
                            openFragmentArc(Preferences.newInstance(), icon, Preferences.TAG)
                        }

                        R.string.menus -> {
                            childFragmentManager.showHomeMenu()
                        }

                        R.string.purchase -> {
                            openFragmentSlide(Trial.newInstance(), Trial.TAG)
                        }
                    }
                }
            })

            navigationRecyclerView.adapter = adapterHome

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
                    childFragmentManager.showAppMenu(packageInfo)
                }
            })

            quickAppsRecyclerView.adapter = adapterQuickApps
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            HomePreferences.HOME_MENU_LAYOUT -> {
                setLayoutManager()
            }
        }
    }

    private fun setLayoutManager() {
        // Clear the adapter
        navigationRecyclerView.adapter = null

        when (HomePreferences.getMenuLayout()) {
            PopupMenuLayout.GRID -> {
                val gridLayoutManager = GridLayoutManager(context, getInteger(R.integer.span_count))

                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (data?.get(position)?.first?.isZero() == true || data?.get(position)?.first == 1) {
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

        navigationRecyclerView.adapter = adapterHome

        navigationRecyclerView.post {
            TransitionManager.beginDelayedTransition(navigationRecyclerView)
        }
    }

    private fun showRateDialog() {
        runCatching {
            if (AppUtils.isPlayFlavor()) {
                if (MainPreferences.shouldShowRateReminder()) {
                    if (TrialPreferences.isFullVersion()) { // Pop up the rate dialog only if the user has the full version
                        requireActivity().showAppReview()
                    }
                }
            }
        }
    }

    override fun setupBackPressedDispatcher() {
        /* no-op */
    }

    companion object {
        fun newInstance(): Home {
            val args = Bundle()
            val fragment = Home()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Home"
    }
}
