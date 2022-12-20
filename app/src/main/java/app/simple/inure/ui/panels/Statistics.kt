package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterUsageStats
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.usagestats.UsageStatsMenu
import app.simple.inure.dialogs.miscellaneous.UsageStatsPermission
import app.simple.inure.dialogs.miscellaneous.UsageStatsPermission.Companion.showUsageStatsPermissionDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.popups.usagestats.PopupAppsCategory
import app.simple.inure.popups.usagestats.PopupUsageStatsSorting
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.viewmodels.panels.UsageStatsViewModel

class Statistics : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterUsageStats: AdapterUsageStats? = null
    private lateinit var usageStatsViewModel: UsageStatsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        recyclerView = view.findViewById(R.id.usage_rv)

        usageStatsViewModel = ViewModelProvider(requireActivity())[UsageStatsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoader()

        if (!requireContext().checkForUsageAccessPermission()) {
            childFragmentManager.showUsageStatsPermissionDialog().setOnUsageStatsPermissionCallbackListener(object : UsageStatsPermission.Companion.UsageStatsPermissionCallbacks {
                override fun onClosedAfterGrant() {
                    adapterUsageStats?.enableLoader()
                    usageStatsViewModel.loadAppStats()
                }
            })
        }

        usageStatsViewModel.usageData.observe(viewLifecycleOwner) {
            postponeEnterTransition()
            hideLoader()

            adapterUsageStats = AdapterUsageStats(it)

            adapterUsageStats?.setOnStatsCallbackListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            recyclerView.adapter = adapterUsageStats

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getAllAppsBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_sort -> {
                        PopupUsageStatsSorting(view)
                    }
                    R.drawable.ic_filter -> {
                        PopupAppsCategory(view)
                    }
                    R.drawable.ic_settings -> {
                        UsageStatsMenu.newInstance()
                            .show(childFragmentManager, "menu")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatisticsPreferences.statsInterval,
            StatisticsPreferences.isUnusedHidden,
            StatisticsPreferences.appsCategory,
            StatisticsPreferences.statsEngine -> {
                adapterUsageStats?.enableLoader()
                usageStatsViewModel.loadAppStats()
            }
            StatisticsPreferences.isSortingReversed,
            StatisticsPreferences.statsSorting -> {
                usageStatsViewModel.sortUsageData()
            }
            StatisticsPreferences.limitHours -> {
                handler.postDelayed(
                        { adapterUsageStats?.notifyAllData() }, 500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(loading: Boolean = false): Statistics {
            val args = Bundle()
            val fragment = Statistics()
            args.putBoolean(BundleConstants.loading, loading)
            fragment.arguments = args
            return fragment
        }
    }
}
