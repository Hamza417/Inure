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
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.dialogs.usagestats.UsageStatsPermission
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.AppStatisticsViewModelFactory
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.viewmodels.viewers.AppStatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet

class UsageStatistics : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var totalTimeUsedChart: BarChart

    private lateinit var appStatisticsViewModel: AppStatisticsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_app_statistics, container, false)

        back = view.findViewById(R.id.app_info_back_button)
        totalTimeUsedChart = view.findViewById(R.id.total_time_used_bar_chart)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        appStatisticsViewModel = ViewModelProvider(this, AppStatisticsViewModelFactory(packageInfo))[AppStatisticsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()
        doPermissionChecks()

        back.setOnClickListener {
            requireActivity().onBackPressed()
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
        appStatisticsViewModel.getTotalUsedChartData().observe(viewLifecycleOwner) {
            totalTimeUsedChart.data = BarData(BarDataSet(it, "Total Time Used"))
            totalTimeUsedChart.notifyDataSetChanged()
            totalTimeUsedChart.invalidate()
        }

        appStatisticsViewModel.getTotalAppSize().observe(requireActivity()) {

        }

        appStatisticsViewModel.error.observe(viewLifecycleOwner) {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): UsageStatistics {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = UsageStatistics()
            fragment.arguments = args
            return fragment
        }
    }
}
