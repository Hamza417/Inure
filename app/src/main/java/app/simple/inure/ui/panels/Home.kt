package app.simple.inure.ui.panels

import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterQuickApps
import app.simple.inure.adapters.menus.AdapterHomeMenu
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.edgeeffect.EdgeEffectNestedScrollView
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.app.ChangesReminder
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.terminal.Term
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.QuickAppsViewModel
import rikka.shizuku.Shizuku

class Home : ScopedFragment() {

    private lateinit var scrollView: EdgeEffectNestedScrollView
    private lateinit var header: PaddingAwareLinearLayout
    private lateinit var navigationRecyclerView: RecyclerView
    private lateinit var quickAppsRecyclerView: CustomHorizontalRecyclerView
    private lateinit var icon: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var purchase: DynamicRippleImageButton

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var quickAppViewModel: QuickAppsViewModel
    private var headerAnimator: ValueAnimator? = null

    private fun getHomeLayout(): Int {
        return if (DevelopmentPreferences.get(DevelopmentPreferences.alternateHomePanel)) {
            R.layout.fragment_home_new
        } else {
            R.layout.fragment_home
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getHomeLayout(), container, false)

        scrollView = view.findViewById(R.id.home_scroll_view)
        quickAppsRecyclerView = view.findViewById(R.id.quick_app_recycler_view)
        navigationRecyclerView = view.findViewById(R.id.home_menu)

        header = view.findViewById(R.id.home_header)
        icon = view.findViewById(R.id.header_icon)
        search = view.findViewById(R.id.home_header_search_button)
        settings = view.findViewById(R.id.home_header_pref_button)
        purchase = view.findViewById(R.id.home_purchase)

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        quickAppViewModel = ViewModelProvider(requireActivity())[QuickAppsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (TrialPreferences.isFullVersion()) {
            purchase.gone(animate = false)
        }

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
                            return if (it[position].first.isZero()) {
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
            requireActivity().supportFragmentManager.beginTransaction()
                .addSharedElement(it, it.transitionName)
                .replace(R.id.app_container, Search.newInstance(firstLaunch = true))
                .addToBackStack("search")
                .commit()
        }

        settings.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .addSharedElement(it, it.transitionName)
                .replace(R.id.app_container, Preferences.newInstance())
                .addToBackStack("preferences")
                .commit()
        }

        purchase.setOnClickListener {
            openFragmentSlide(Trial.newInstance(), "trial")
        }

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                headerAnimator?.cancel()

                headerAnimator = ValueAnimator.ofFloat(header.elevation, 10F)
                headerAnimator?.addUpdateListener {
                    header.elevation = it.animatedValue as Float
                }
                headerAnimator?.interpolator = DecelerateInterpolator()
                headerAnimator?.start()
            } else if (scrollY <= 0) {
                headerAnimator?.cancel()

                headerAnimator = ValueAnimator.ofFloat(header.elevation, 0F)
                headerAnimator?.addUpdateListener {
                    header.elevation = it.animatedValue as Float
                }
                headerAnimator?.interpolator = DecelerateInterpolator()
                headerAnimator?.start()
            }
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