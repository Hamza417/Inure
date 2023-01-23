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
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.AppStatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.concurrent.TimeUnit

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
            barChart.data = BarDataSet(it, "Daily Usage Statistics").let { dataSet ->
                dataSet.valueFormatter = AxisFormatter()
                dataSet.isHighlightEnabled = true
                dataSet.highLightColor = AppearancePreferences.getAccentColorLight()
                dataSet.color = AppearancePreferences.getAccentColor()
                dataSet.valueTextColor = ThemeManager.theme.textViewTheme.secondaryTextColor
                dataSet.valueTypeface = TypeFace.getMediumTypeFace(requireContext())
                dataSet.formLineWidth = 0f

                BarData(dataSet)
            }
            barChart.setDrawBarShadow(false)
            barChart.setDrawBorders(false)
            barChart.setDrawGridBackground(false)
            barChart.setFitBars(true)
            barChart.description.isEnabled = false
            barChart.axisLeft.isEnabled = false
            barChart.axisRight.isEnabled = false
            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM // set x axis position to bottom
            barChart.xAxis.setDrawGridLines(false)
            barChart.xAxis.setDrawLabels(true)

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

    inner class AxisFormatter : ValueFormatter() {
        private val days = arrayOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()) ?: value.toString()
        }

        override fun getBarLabel(barEntry: BarEntry?): String {
            return when {
                TimeUnit.MILLISECONDS.toSeconds(barEntry!!.y.toLong()) < 60 -> {
                    getString(R.string.for_seconds,
                              TimeUnit.MILLISECONDS.toSeconds(barEntry.y.toLong()).toString())
                }
                TimeUnit.MILLISECONDS.toMinutes(barEntry.y.toLong()) < 60 -> {
                    getString(R.string.for_short,
                              TimeUnit.MILLISECONDS.toMinutes(barEntry.y.toLong()).toString())
                }
                TimeUnit.MILLISECONDS.toHours(barEntry.y.toLong()) < 24 -> {
                    getString(R.string.for_long,
                              TimeUnit.MILLISECONDS.toHours(barEntry.y.toLong()).toString(),
                              (TimeUnit.MILLISECONDS.toMinutes(barEntry.y.toLong()) % 60).toString())
                }
                else -> {
                    getString(R.string.for_days,
                              TimeUnit.MILLISECONDS.toDays(barEntry.y.toLong()).toString(),
                              TimeUnit.MILLISECONDS.toHours(barEntry.y.toLong()).toString(),
                              (TimeUnit.MILLISECONDS.toMinutes(barEntry.y.toLong()) % 60).toString())
                }
            }
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