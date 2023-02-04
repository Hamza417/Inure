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
import app.simple.inure.decorations.theme.ThemeBarChart
import app.simple.inure.decorations.theme.ThemePieChart
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ChartMarkerView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.UsageStatsPermission
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.AppStatisticsViewModelFactory
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.AppStatisticsGraphViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.concurrent.TimeUnit

class UsageStatisticsGraph : ScopedFragment() {

    private lateinit var screenTime: TypeFaceTextView
    private lateinit var launchCount: TypeFaceTextView
    private lateinit var lastUsed: TypeFaceTextView
    private lateinit var mobileData: TypeFaceTextView
    private lateinit var wifiData: TypeFaceTextView
    private lateinit var barChart: ThemeBarChart
    private lateinit var pieChart: ThemePieChart
    private lateinit var back: DynamicRippleImageButton
    private lateinit var loader: CustomProgressBar

    private lateinit var appStatisticsGraphViewModel: AppStatisticsGraphViewModel

    private var barEntries = ArrayList<BarEntry>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_app_statistics_graph, container, false)

        screenTime = view.findViewById(R.id.screen_time)
        launchCount = view.findViewById(R.id.launched)
        lastUsed = view.findViewById(R.id.last_used)
        mobileData = view.findViewById(R.id.mobile_data)
        wifiData = view.findViewById(R.id.wifi_data)
        barChart = view.findViewById(R.id.bar_chart)
        pieChart = view.findViewById(R.id.pie_chart)
        back = view.findViewById(R.id.app_info_back_button)
        loader = view.findViewById(R.id.loader)

        appStatisticsGraphViewModel = ViewModelProvider(this, AppStatisticsViewModelFactory(packageInfo))[AppStatisticsGraphViewModel::class.java]

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
        appStatisticsGraphViewModel.getPackageStats().observe(viewLifecycleOwner) {
            with(it.totalTimeUsed) {
                screenTime.apply {
                    this.setTextWithAnimation(
                            when {
                                TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                                    this.context.getString(R.string.used_for_seconds,
                                                           TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                                }
                                TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                                    this.context.getString(R.string.used_for_short,
                                                           TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                                }
                                TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                                    this.context.getString(R.string.used_for_long,
                                                           TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                           (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                }
                                else -> {
                                    this.context.getString(R.string.used_for_days,
                                                           TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                           (TimeUnit.MILLISECONDS.toHours(this@with) % 24).toString(),
                                                           (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                }
                            })
                }
            }

            with(System.currentTimeMillis() - it.appUsage!![0].date) {
                lastUsed.apply {
                    this.setTextWithAnimation(
                            when {
                                TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                                    this.context.getString(R.string.last_used_seconds,
                                                           TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                                }
                                TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                                    this.context.getString(R.string.last_used_short,
                                                           TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                                }
                                TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                                    this.context.getString(R.string.last_used_long,
                                                           TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                           (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                }
                                else -> {
                                    this.context.getString(R.string.last_used_days,
                                                           TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                           (TimeUnit.MILLISECONDS.toHours(this@with) % 24).toString(),
                                                           (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                                }
                            })
                }
            }

            launchCount.setTextWithAnimation(getString(R.string.times, it.launchCount))
            mobileData.setTextWithAnimation(it.mobileData.toString())
            wifiData.setTextWithAnimation(it.wifiData.toString())
        }

        appStatisticsGraphViewModel.getChartData().observe(viewLifecycleOwner) {
            barChart.data = BarDataSet(it, "").let { dataSet ->
                dataSet.valueFormatter = AxisFormatter()
                dataSet.isHighlightEnabled = true
                dataSet.colors = BAR_COLORS.reversed()
                dataSet.valueTypeface = TypeFace.getMediumTypeFace(requireContext())
                dataSet.formLineWidth = 0f

                BarData(dataSet)
            }

            barEntries = it
            barChart.xAxis.valueFormatter = XAxisFormatter()
            barChart.isKeepPositionOnRotation = true

            barChart.invalidate()
            barChart.animateY(1000, Easing.EaseOutCubic)

            loader.gone(animate = true)
        }

        appStatisticsGraphViewModel.getPieChartData().observe(viewLifecycleOwner) {
            pieChart.apply {
                PieDataSet(it, "").apply {
                    data = PieData(this)
                    colors = BAR_COLORS.reversed()
                    valueTextColor = ThemeManager.theme.textViewTheme.primaryTextColor
                    valueTextSize = 9F
                    valueTypeface = TypeFace.getBoldTypeFace(requireContext())
                    setEntryLabelColor(ThemeManager.theme.textViewTheme.primaryTextColor)
                    setEntryLabelTextSize(9F)
                    setEntryLabelTypeface(TypeFace.getBoldTypeFace(requireContext()))
                    valueFormatter = PercentFormatter(pieChart)
                    setDrawIcons(true)
                    xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                }

                animateXY(1000, 500, Easing.EaseOutCubic)
            }

            pieChart.setUsePercentValues(true)
            pieChart.setAnimation(false)
            pieChart.notifyDataSetChanged()
            pieChart.notifyDataSetChanged()
            pieChart.invalidate()
            pieChart.marker = ChartMarkerView(requireContext(), R.layout.marker_view)
        }

        appStatisticsGraphViewModel.getError().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showError(it)
        }

        appStatisticsGraphViewModel.warning.observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            showWarning(it)
        }
    }

    inner class AxisFormatter : ValueFormatter() {
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

    inner class XAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val todayNumber = CalendarUtils.getWeekNumberFromDate(System.currentTimeMillis())
            val dayValue = (value + todayNumber).toInt() % 7 // Offset the day value by today's day
            return barEntries.getOrNull(6 - value.toInt())?.data?.toString() ?: getString(R.string.not_available)
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

        private val days = arrayOf(
                R.string.sun,
                R.string.mon,
                R.string.tue,
                R.string.wed,
                R.string.thu,
                R.string.fri,
                R.string.sat)

        val BAR_COLORS = arrayListOf(
                ColorTemplate.rgb("#DC828F"), // New York Pink
                ColorTemplate.rgb("#F7CE76"), // Rajah
                ColorTemplate.rgb("#E8D6CF"), // Dust Storm
                ColorTemplate.rgb("#8C7386"), // Mountbatten Pink
                ColorTemplate.rgb("#698396"), // Lynch
                ColorTemplate.rgb("#8FA2A6"), // Light Slate Grey
                ColorTemplate.rgb("#874741"), // Sienna
        )
    }
}