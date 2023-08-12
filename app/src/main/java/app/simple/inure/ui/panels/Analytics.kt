package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.analytics.AdapterLegend
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemePieChart
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LegendRecyclerView
import app.simple.inure.dialogs.analytics.AnalyticsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.charts.PopupChartEntry
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.ui.subpanels.AnalyticsMinimumSDK
import app.simple.inure.ui.subpanels.AnalyticsPackageType
import app.simple.inure.ui.subpanels.AnalyticsTargetSDK
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.AnalyticsViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate

class Analytics : ScopedFragment() {

    private lateinit var settings: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var minSdkHeading: TypeFaceTextView
    private lateinit var minimumOsPie: ThemePieChart
    private lateinit var targetOsPie: ThemePieChart
    private lateinit var installLocationPie: ThemePieChart
    private lateinit var packageTypePie: ThemePieChart
    private lateinit var minimumOsLegend: LegendRecyclerView
    private lateinit var targetOsLegend: LegendRecyclerView
    private lateinit var installLocationLegend: LegendRecyclerView
    private lateinit var packageTypeLegend: LegendRecyclerView

    private val analyticsViewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        settings = view.findViewById(R.id.configuration_button)
        search = view.findViewById(R.id.search_button)
        minSdkHeading = view.findViewById(R.id.min_sdk_heading)
        minimumOsPie = view.findViewById(R.id.minimum_os_pie)
        targetOsPie = view.findViewById(R.id.target_os_pie)
        installLocationPie = view.findViewById(R.id.install_location_pie)
        packageTypePie = view.findViewById(R.id.package_type_pie)
        minimumOsLegend = view.findViewById(R.id.minimum_os_legend)
        targetOsLegend = view.findViewById(R.id.target_os_legend)
        installLocationLegend = view.findViewById(R.id.install_location_legend)
        packageTypeLegend = view.findViewById(R.id.package_type_legend)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            minimumOsPie.gone()
            minSdkHeading.gone()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        analyticsViewModel.getMinimumOsData().observe(viewLifecycleOwner) { pieData ->
            minimumOsPie.apply {
                PieDataSet(pieData.first, "").apply {
                    data = PieData(this)
                    colors = pieData.second
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            openFragmentSlide(AnalyticsMinimumSDK.newInstance(it), "sdk")
                        }.setOnDismissListener {
                            minimumOsPie.highlightValues(null)
                        }
                    }
                })

                val adapter = AdapterLegend(pieData.first, pieData.second) { pieEntry, longPressed ->
                    if (longPressed) {
                        openFragmentSlide(AnalyticsMinimumSDK.newInstance(pieEntry), "sdk")
                    } else {
                        minimumOsPie.highlightValue(Highlight(
                                pieData.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    }
                }

                minimumOsLegend.adapter = adapter
            }

            minimumOsPie.setAnimation(true)
            minimumOsPie.notifyDataSetChanged()
            minimumOsPie.invalidate()
            //            minimumOsPie.marker = ChartMarkerView(requireContext(), R.layout.marker_view) {
            //                openFragmentSlide(AnalyticsMinimumSDK.newInstance(it), "sdk")
            //            }
        }

        analyticsViewModel.getTargetSDKData().observe(viewLifecycleOwner) {
            targetOsPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = it.second
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            openFragmentSlide(AnalyticsTargetSDK.newInstance(it), "target_sdk")
                        }.setOnDismissListener {
                            targetOsPie.highlightValues(null)
                        }
                    }
                })

                val adapter = AdapterLegend(it.first, it.second) { pieEntry, longPressed ->
                    if (longPressed) {
                        openFragmentSlide(AnalyticsTargetSDK.newInstance(pieEntry), "target_sdk")
                    } else {
                        targetOsPie.highlightValue(Highlight(
                                it.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    }
                }

                targetOsLegend.adapter = adapter
            }

            targetOsPie.setAnimation(false)
            targetOsPie.notifyDataSetChanged()
            targetOsPie.invalidate()
        }

        analyticsViewModel.getInstallLocationData().observe(viewLifecycleOwner) {
            installLocationPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = ColorTemplate.PASTEL_COLORS.toMutableList()
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            // openFragmentSlide(AnalyticsPackageType.newInstance(it), "package_type")
                        }.setOnDismissListener {
                            installLocationPie.highlightValues(null)
                        }
                    }
                })

                val adapter = AdapterLegend(it.first, ColorTemplate.PASTEL_COLORS.toMutableList().toArrayList()) { pieEntry, longPressed ->
                    if (longPressed) {
                        // openFragmentSlide(AnalyticsPackageType.newInstance(pieEntry), "package_type")
                    } else {
                        installLocationPie.highlightValue(Highlight(
                                it.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    }
                }

                installLocationLegend.adapter = adapter
            }

            installLocationPie.setAnimation(false)
            installLocationPie.notifyDataSetChanged()
            installLocationPie.invalidate()
        }

        analyticsViewModel.getPackageTypeData().observe(viewLifecycleOwner) {
            packageTypePie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = ColorTemplate.PASTEL_COLORS.toMutableList()
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        /* no-op */
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        PopupChartEntry(view, e) {
                            openFragmentSlide(AnalyticsPackageType.newInstance(it), "package_type")
                        }.setOnDismissListener {
                            packageTypePie.highlightValues(null)
                        }
                    }
                })

                val adapter = AdapterLegend(it.first, ColorTemplate.PASTEL_COLORS.toMutableList().toArrayList()) { pieEntry, longPressed ->
                    if (longPressed) {
                        openFragmentSlide(AnalyticsPackageType.newInstance(pieEntry), "package_type")
                    } else {
                        packageTypePie.highlightValue(Highlight(
                                it.first.indexOf(pieEntry).toFloat(),
                                0, 0), false)
                    }
                }

                packageTypeLegend.adapter = adapter
            }

            packageTypePie.setAnimation(false)
            packageTypePie.notifyDataSetChanged()
            packageTypePie.invalidate()
        }

        settings.setOnClickListener {
            AnalyticsMenu.newInstance()
                .show(childFragmentManager, "analytics_menu")
        }

        search.setOnClickListener {
            openFragmentSlide(Search.newInstance(true), "preferences_screen")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AnalyticsPreferences.sdkValue -> {
                analyticsViewModel.refreshPackageData()
            }
        }
    }

    companion object {
        fun newInstance(): Analytics {
            val args = Bundle()
            val fragment = Analytics()
            fragment.arguments = args
            return fragment
        }
    }
}