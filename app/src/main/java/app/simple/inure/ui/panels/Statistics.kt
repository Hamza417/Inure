package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.StatisticsAdapter
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.Preparing
import app.simple.inure.dialogs.usagestats.UsageStatsMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupMainList
import app.simple.inure.preferences.StatsPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.UsageStatsData

class Statistics : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var statisticsAdapter: StatisticsAdapter
    private val usageStatsData: UsageStatsData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        recyclerView = view.findViewById(R.id.usage_rv)

        statisticsAdapter = StatisticsAdapter()
        recyclerView.adapter = statisticsAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usageStatsData.usageData.observe(viewLifecycleOwner, {
            postponeEnterTransition()

            statisticsAdapter.setOnStatsCallbackListener(object : StatisticsAdapter.Companion.StatsAdapterCallbacks {
                override fun onFilterPressed(view: View) {
                    UsageStatsMenu.newInstance()
                            .show(childFragmentManager, "menu")
                }

                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo.applicationInfo, icon)
                }

                override fun onAppLongClicked(packageInfo: PackageInfo, icon: ImageView, anchor: ViewGroup) {
                    val popupMenu = PopupMainList(layoutInflater.inflate(R.layout.popup_main_list, PopupLinearLayout(requireContext()), true),
                                                  packageInfo.applicationInfo, anchor)

                    popupMenu.setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String, applicationInfo: ApplicationInfo) {
                            when (source) {
                                getString(R.string.app_information) -> {
                                    openAppInfo(applicationInfo, icon)
                                }
                                getString(R.string.send) -> {
                                    Preparing.newInstance(applicationInfo)
                                            .show(parentFragmentManager, "send_app")
                                }
                            }
                        }
                    })
                }
            })

            statisticsAdapter.setData(it).also {
                recyclerView.setupFastScroller()
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })
    }

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(applicationInfo, icon.transitionName),
                                    icon, "app_info")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            StatsPreferences.statsInterval -> {
                usageStatsData.loadAppStats()
            }
            StatsPreferences.appsCategory -> {
                usageStatsData.loadAppStats()
            }
            StatsPreferences.isSortingReversed,
            StatsPreferences.statsSorting,
            -> {
                usageStatsData.sortUsageData()
            }
        }
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
