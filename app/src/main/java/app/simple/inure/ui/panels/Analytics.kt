package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.analytics.AdapterInstallerLegend
import app.simple.inure.adapters.analytics.AdapterLegend
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.SortConstant
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.theme.ThemePieChart
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LegendRecyclerView
import app.simple.inure.dialogs.analytics.AnalyticsMenu.Companion.showAnalyticsMenu
import app.simple.inure.dialogs.analytics.AnalyticsSort.Companion.showAnalyticsSort
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.charts.PopupChartEntry
import app.simple.inure.popups.charts.PopupInstallerChartEntry
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.ui.subpanels.AnalyticsInstaller
import app.simple.inure.ui.subpanels.AnalyticsMinimumSDK
import app.simple.inure.ui.subpanels.AnalyticsPackageType
import app.simple.inure.ui.subpanels.AnalyticsTargetSDK
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.AnalyticsViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate

class Analytics : ScopedFragment() {

    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var filterStyle: TypeFaceTextView
    private lateinit var minSdkHeading: TypeFaceTextView
    private lateinit var minimumOsPie: ThemePieChart
    private lateinit var targetOsPie: ThemePieChart
    private lateinit var packageTypePie: ThemePieChart
    private lateinit var installerPie: ThemePieChart
    private lateinit var minimumOsLegend: LegendRecyclerView
    private lateinit var targetOsLegend: LegendRecyclerView
    private lateinit var packageTypeLegend: LegendRecyclerView
    private lateinit var installerLegend: LegendRecyclerView

    private val analyticsViewModel: AnalyticsViewModel by viewModels()

    private var minimumOS: AdapterLegend? = null
    private var targetOS: AdapterLegend? = null
    private var packageTypeAdapter: AdapterLegend? = null
    private var installerAdapter: AdapterInstallerLegend? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        scrollView = view.findViewById(R.id.scroll_view)
        filterStyle = view.findViewById(R.id.filter_style)
        minSdkHeading = view.findViewById(R.id.min_sdk_heading)
        minimumOsPie = view.findViewById(R.id.minimum_os_pie)
        targetOsPie = view.findViewById(R.id.target_os_pie)
        packageTypePie = view.findViewById(R.id.package_type_pie)
        minimumOsLegend = view.findViewById(R.id.minimum_os_legend)
        targetOsLegend = view.findViewById(R.id.target_os_legend)
        packageTypeLegend = view.findViewById(R.id.package_type_legend)
        installerPie = view.findViewById(R.id.installer_pie)
        installerLegend = view.findViewById(R.id.installer_legend)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            minimumOsPie.gone()
            minSdkHeading.gone()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFilterStyle()
        startPostponedEnterTransition()

        bottomRightCornerMenu?.initBottomMenuWithScrollView(
                BottomMenuConstants.getAllAppsBottomMenuItems(), scrollView) { id, _ ->
            when (id) {
                R.drawable.ic_settings -> {
                    childFragmentManager.showAnalyticsMenu()
                }
                R.drawable.ic_search -> {
                    openFragmentSlide(Search.newInstance(true), "search")
                }
                R.drawable.ic_refresh -> {
                    showLoader(manualOverride = true)
                    analyticsViewModel.refreshPackageData()
                }
                R.drawable.ic_filter -> {
                    childFragmentManager.showAnalyticsSort()
                }
            }
        }

        analyticsViewModel.getMinimumOsData().observe(viewLifecycleOwner) { pieData ->
            hideLoader()

            minimumOsPie.apply {
                PieDataSet(pieData.first, "").apply {
                    data = PieData(this)
                    colors = pieData.second
                    checkLabelState()
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            openFragmentSlide(AnalyticsMinimumSDK.newInstance(it), AnalyticsMinimumSDK.TAG)
                        }.setOnDismissListener {
                            runCatching {
                                minimumOsPie.highlightValues(null)
                                minimumOS?.highlightEntry(null)
                            }
                        }

                        runCatching {
                            minimumOS?.highlightEntry(e as PieEntry?)
                        }
                    }
                })

                minimumOS = AdapterLegend(pieData.first, pieData.second) { pieEntry, longPressed ->
                    if (longPressed) {
                        minimumOsPie.highlightValue(Highlight(
                                pieData.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    } else {
                        openFragmentSlide(AnalyticsMinimumSDK.newInstance(pieEntry), AnalyticsMinimumSDK.TAG)
                    }
                }

                minimumOsLegend.adapter = minimumOS
            }

            minimumOsPie.setAnimation(true)
            minimumOsPie.startAnimation()
            //            minimumOsPie.marker = ChartMarkerView(requireContext(), R.layout.marker_view) {
            //                openFragmentSlide(AnalyticsMinimumSDK.newInstance(it), "sdk")
            //            }
        }

        analyticsViewModel.getTargetSDKData().observe(viewLifecycleOwner) {
            hideLoader()

            targetOsPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = it.second
                    checkLabelState()
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            openFragmentSlide(AnalyticsTargetSDK.newInstance(it), AnalyticsTargetSDK.TAG)
                        }.setOnDismissListener {
                            runCatching {
                                targetOsPie.highlightValues(null)
                                targetOS?.highlightEntry(null)
                            }
                        }

                        runCatching {
                            targetOS?.highlightEntry(e as PieEntry?)
                        }
                    }
                })

                targetOS = AdapterLegend(it.first, it.second) { pieEntry, longPressed ->
                    if (longPressed) {
                        targetOsPie.highlightValue(Highlight(
                                it.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    } else {
                        openFragmentSlide(AnalyticsTargetSDK.newInstance(pieEntry), AnalyticsTargetSDK.TAG)
                    }
                }

                targetOsLegend.adapter = targetOS
            }

            targetOsPie.setAnimation(true)
            targetOsPie.invalidate()
        }

        analyticsViewModel.getPackageTypeData().observe(viewLifecycleOwner) {
            hideLoader()

            packageTypePie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = ColorTemplate.PASTEL_COLORS.toMutableList()
                    checkLabelState()
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            openFragmentSlide(AnalyticsPackageType.newInstance(it), AnalyticsPackageType.TAG)
                        }.setOnDismissListener {
                            runCatching {
                                packageTypePie.highlightValues(null)
                                packageTypeAdapter?.highlightEntry(null)
                            }
                        }

                        runCatching {
                            packageTypeAdapter?.highlightEntry(e as PieEntry?)
                        }
                    }
                })

                packageTypeAdapter = AdapterLegend(
                        it.first, ColorTemplate.PASTEL_COLORS.toMutableList().toArrayList()) { pieEntry, longPressed ->
                    if (longPressed) {
                        packageTypePie.highlightValue(Highlight(
                                it.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    } else {
                        openFragmentSlide(AnalyticsPackageType.newInstance(pieEntry), AnalyticsPackageType.TAG)
                    }
                }

                packageTypeLegend.adapter = packageTypeAdapter
            }

            packageTypePie.setAnimation(true)
            packageTypePie.notifyDataSetChanged()
            packageTypePie.invalidate()
        }

        analyticsViewModel.getInstallerData().observe(viewLifecycleOwner) {
            hideLoader()

            installerPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = it.second
                    checkLabelState()
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupInstallerChartEntry(view, e, it.third) {
                            openFragmentSlide(AnalyticsInstaller.newInstance(it), AnalyticsInstaller.TAG)
                        }.setOnDismissListener {
                            runCatching {
                                installerPie.highlightValues(null)
                                installerAdapter?.highlightEntry(null)
                            }
                        }

                        runCatching {
                            installerAdapter?.highlightEntry(e as PieEntry?)
                        }
                    }
                })

                installerAdapter = AdapterInstallerLegend(it.first, it.second, it.third) { pieEntry, longPressed ->
                    if (longPressed) {
                        installerPie.highlightValue(Highlight(
                                it.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    } else {
                        openFragmentSlide(AnalyticsInstaller.newInstance(pieEntry), AnalyticsInstaller.TAG)
                    }
                }

                installerLegend.adapter = installerAdapter
            }

            installerPie.setAnimation(true)
            installerPie.notifyDataSetChanged()
            installerPie.invalidate()
        }
    }

    private fun setFilterStyle() {
        filterStyle.text = when (AnalyticsPreferences.getApplicationType()) {
            SortConstant.USER -> {
                getString(R.string.user)
            }
            SortConstant.SYSTEM -> {
                getString(R.string.system)
            }
            SortConstant.BOTH -> {
                with(StringBuilder()) {
                    append(getString(R.string.user))
                    append(" | ")
                    append(getString(R.string.system))
                }
            }
            else -> {
                getString(R.string.unknown)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AnalyticsPreferences.SDK_VALUE -> {
                analyticsViewModel.refreshPackageData()
            }
            AnalyticsPreferences.APPLICATION_TYPE -> {
                analyticsViewModel.refreshPackageData()
                setFilterStyle()
            }
            AnalyticsPreferences.CHART_LABEL -> {
                minimumOsPie.checkLabelState()
                targetOsPie.checkLabelState()
                packageTypePie.checkLabelState()
                installerPie.checkLabelState()
            }
        }
    }

    private fun ThemePieChart.checkLabelState() {
        if (AnalyticsPreferences.getChartLabel()) {
            enableChartLabels(existingDataSet)
        } else {
            disableChartLabels(existingDataSet)
        }
    }

    companion object {
        fun newInstance(): Analytics {
            val args = Bundle()
            val fragment = Analytics()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Analytics"
    }
}
