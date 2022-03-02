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
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.menus.UsageStatsMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.usagestats.PopupAppsCategory
import app.simple.inure.popups.usagestats.PopupUsageStatsSorting
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.UsageStatsViewModel

class Statistics : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterUsageStats: AdapterUsageStats
    private lateinit var usageStatsViewModel: UsageStatsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        recyclerView = view.findViewById(R.id.usage_rv)

        usageStatsViewModel = ViewModelProvider(requireActivity())[UsageStatsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usageStatsViewModel.usageData.observe(viewLifecycleOwner) {
            postponeEnterTransition()

            adapterUsageStats = AdapterUsageStats(it)

            adapterUsageStats.setOnStatsCallbackListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }

                override fun onSortPressed(view: View) {
                    PopupUsageStatsSorting(view)
                }

                override fun onFilterPressed(view: View) {
                    PopupAppsCategory(view)
                }

                override fun onSettingsPressed(view: View) {
                    UsageStatsMenu.newInstance()
                        .show(childFragmentManager, "menu")
                }

                override fun onSearchPressed(view: View) {
                    clearTransitions()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                Search.newInstance(true),
                                                "search")
                }
            })

            recyclerView.adapter = adapterUsageStats

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    private fun openAppInfo(applicationInfo: PackageInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(applicationInfo, icon.transitionName),
                                    icon, "app_info")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatisticsPreferences.statsInterval,
            StatisticsPreferences.isUnusedHidden,
            StatisticsPreferences.appsCategory -> {
                usageStatsViewModel.loadAppStats()
            }
            StatisticsPreferences.isSortingReversed,
            StatisticsPreferences.statsSorting -> {
                usageStatsViewModel.sortUsageData()
            }
            StatisticsPreferences.limitHours -> {
                handler.postDelayed(
                        { adapterUsageStats.notifyAllData() }, 500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(): Statistics {
            val args = Bundle()
            val fragment = Statistics()
            fragment.arguments = args
            return fragment
        }
    }
}
