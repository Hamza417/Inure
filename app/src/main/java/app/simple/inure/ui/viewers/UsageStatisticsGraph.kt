package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.analytics.AdapterLegend
import app.simple.inure.adapters.analytics.AdapterLegendBar
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeBarChart
import app.simple.inure.decorations.theme.ThemePieChart
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.decorations.views.LegendRecyclerView
import app.simple.inure.dialogs.miscellaneous.UsageStatsPermission
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.AppStatisticsViewModelFactory
import app.simple.inure.popups.charts.PopupChartEntry
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.TypeFace
import app.simple.inure.util.ViewUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.AppStatisticsGraphViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.concurrent.TimeUnit

class UsageStatisticsGraph : ScopedFragment() {

    private lateinit var screenTime: TypeFaceTextView
    private lateinit var launchCount: TypeFaceTextView
    private lateinit var lastUsed: TypeFaceTextView
    private lateinit var mobileData: TypeFaceTextView
    private lateinit var wifiData: TypeFaceTextView
    private lateinit var barChart: ThemeBarChart
    private lateinit var barChartLegend: LegendRecyclerView
    private lateinit var pieChart: ThemePieChart
    private lateinit var pieChartLegend: LegendRecyclerView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var loader: CustomProgressBar

    private lateinit var appStatisticsGraphViewModel: AppStatisticsGraphViewModel

    private var barEntries = ArrayList<BarEntry>()
    private var pieEntries = ArrayList<PieEntry>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_app_statistics_graph, container, false)

        screenTime = view.findViewById(R.id.screen_time)
        launchCount = view.findViewById(R.id.launched)
        lastUsed = view.findViewById(R.id.last_used)
        mobileData = view.findViewById(R.id.mobile_data)
        wifiData = view.findViewById(R.id.wifi_data)
        barChart = view.findViewById(R.id.bar_chart)
        barChartLegend = view.findViewById(R.id.stats_legend_bar)
        pieChart = view.findViewById(R.id.pie_chart)
        pieChartLegend = view.findViewById(R.id.stats_legend_pie)
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
                    this.setTextWithSlideAnimation(
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
                            }, 250L, ViewUtils.LEFT, 0L)
                }
            }

            launchCount.setTextWithSlideAnimation(getString(R.string.times, it.launchCount), 250L, ViewUtils.LEFT, 50L)

            with(System.currentTimeMillis() - it.appUsage!![0].date) {
                lastUsed.apply {
                    this.setTextWithSlideAnimation(
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
                            }, 250L, ViewUtils.LEFT, 100L)
                }
            }

            mobileData.setTextWithSlideAnimation(it.mobileData.toString(), 250L, ViewUtils.LEFT, 150L)
            wifiData.setTextWithSlideAnimation(it.wifiData.toString(), 250L, ViewUtils.LEFT, 200L)
        }

        appStatisticsGraphViewModel.getChartData().observe(viewLifecycleOwner) {
            barChart.data = BarDataSet(it, "").let { dataSet ->
                dataSet.valueFormatter = AxisFormatter()
                dataSet.isHighlightEnabled = true
                dataSet.colors = BAR_COLORS.reversed()
                dataSet.valueTypeface = TypeFace.getMediumTypeFace(requireContext())
                dataSet.formLineWidth = 0F

                BarData(dataSet)
            }

            barEntries = it
            barChart.xAxis.valueFormatter = XAxisFormatter()
            barChart.isKeepPositionOnRotation = true
            barChart.legend.isEnabled = false

            with(it) {
                val legendEntries = ArrayList<LegendEntry>()
                for (i in 0 until size) {
                    legendEntries.add(LegendEntry().apply {
                        label = this@with[i].data.toString()
                        formColor = BAR_COLORS.reversed()[i]
                    })
                }
                barChart.legend.setCustom(legendEntries.reversed())
            }

            val adapter = AdapterLegendBar(it, BAR_COLORS.reversed().toArrayList()) { barEntry, longPressed ->
                if (longPressed) {
                    // openFragmentSlide(AnalyticsPackageType.newInstance(barEntry), "package_type")
                    Log.d("UsageStatisticsGraph", "onValueSelected: ${barEntry.data}")
                } else {
                    barChart.highlightValue(
                            Highlight( /* I think reversing the list will do as well  but we'll just subtract the sizes */
                                       it.size.toFloat().minus(1) - it.indexOf(barEntry).toFloat(),
                                       0, 0), false)

                    pieChart.highlightValue(Highlight(it.indexOf(barEntry).toFloat(), 0, 0), false)
                }
            }

            barChartLegend.adapter = adapter

            barChart.animateY(1000, Easing.EaseOutCubic)

            loader.gone(animate = true)
        }

        appStatisticsGraphViewModel.getPieChartData().observe(viewLifecycleOwner) {
            pieEntries = it

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

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view!!, e).setOnDismissListener {
                            pieChart.highlightValues(null)
                        }
                    }
                })

                val adapter = AdapterLegend(it, BAR_COLORS.reversed().toArrayList()) { pieEntry, longPressed ->
                    if (longPressed) {
                        // openFragmentSlide(AnalyticsPackageType.newInstance(pieEntry), "package_type")
                    } else {
                        pieChart.highlightValue(Highlight(
                                it.indexOf(pieEntry).toFloat(),
                                0, 0), false)

                        barChart.highlightValue(
                                Highlight( /* I think reversing the list will do as well  but we'll just subtract the sizes */
                                           pieEntries.size.minus(1) - it.indexOf(pieEntry).toFloat(),
                                           0, 0), false)
                    }
                }

                pieChartLegend.adapter = adapter
            }

            pieChart.setUsePercentValues(true)
            pieChart.setAnimation(false)
            pieChart.notifyDataSetChanged()
            pieChart.invalidate()
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
            // val todayNumber = CalendarUtils.getWeekNumberFromDate(System.currentTimeMillis())
            // val dayValue = (value + todayNumber).toInt() % 7 // Offset the day value by today's day
            return barEntries.getOrNull(barEntries.size.minus(1) - value.toInt())?.data?.toString() ?: getString(R.string.not_available)
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

        val BAR_COLORS = arrayListOf(
                ColorTemplate.rgb("#576F72"), // Cadet
                ColorTemplate.rgb("#F7CE76"), // Rajah
                ColorTemplate.rgb("#E8D6CF"), // Dust Storm
                ColorTemplate.rgb("#8C7386"), // Mountbatten Pink
                ColorTemplate.rgb("#698396"), // Lynch
                ColorTemplate.rgb("#8FA2A6"), // Light Slate Grey
                ColorTemplate.rgb("#874741"), // Sienna
                ColorTemplate.rgb("#D0C9C0"), // Pastel Gray (Light Gray)
                /**
                 * Safe zone colors, usually not used due to the nature of the data
                 * being restricted to the last 7 days only. However, in some cases
                 * the data may be more than 7 days, so these colors are used to
                 * compensate for that.
                 */
                ColorTemplate.rgb("#D5B4B4"),
                ColorTemplate.rgb("#94AF9F"),
                ColorTemplate.rgb("#DFD3C3"),
                ColorTemplate.rgb("#65647C"),
                ColorTemplate.rgb("#AEBDCA"),
                ColorTemplate.rgb("#F2D7D9"),
        )
    }
}