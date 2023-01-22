package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.UsageStatsPermission
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.AppStatisticsViewModelFactory
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.AppStatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet

class UsageStatisticsGraph : ScopedFragment() {

    private lateinit var barChart: BarChart
    private lateinit var back: DynamicRippleImageButton
    private lateinit var loader: CustomProgressBar

    private lateinit var appStatisticsViewModel: AppStatisticsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_app_statistics_graph, container, false)

        barChart = view.findViewById(R.id.bar_chart)
        back = view.findViewById(R.id.app_info_back_button)
        loader = view.findViewById(R.id.loader)

        appStatisticsViewModel = ViewModelProvider(this, AppStatisticsViewModelFactory(packageInfo))[AppStatisticsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()
        startPostponedEnterTransition()
        doPermissionChecks()

        back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun doPermissionChecks() {
        if (requireContext().checkForUsageAccessPermission()) {
            observeData()
        } else {
            val dialog = UsageStatsPermission.newInstance()

            dialog.setOnUsageStatsPermissionCallbackListener(object : UsageStatsPermission.Companion.UsageStatsPermissionCallbacks {
                override fun onClosedAfterGrant() {
                    observeData()
                }
            })

            dialog.show(childFragmentManager, "usage_stats_permission")
        }
    }

    private fun observeData() {
        appStatisticsViewModel.getChartData().observe(viewLifecycleOwner) {
            barChart.data = BarDataSet(it, "Usage").let { dataSet ->
                dataSet.valueTextSize = 10f
                dataSet.setDrawValues(false)
                BarData(dataSet)
            }
            barChart.invalidate()
            loader.gone(animate = true)
        }

        appStatisticsViewModel.getError().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showError(it)
        }

        appStatisticsViewModel.warning.observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showWarning(it)
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): UsageStatisticsGraph {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = UsageStatisticsGraph()
            fragment.arguments = args
            return fragment
        }
    }
}