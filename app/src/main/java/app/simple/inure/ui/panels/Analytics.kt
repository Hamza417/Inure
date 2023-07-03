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
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemePieChart
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.ChartMarkerView
import app.simple.inure.dialogs.analytics.AnalyticsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.ui.subpanels.AnalyticsMinimumSDK
import app.simple.inure.ui.subpanels.AnalyticsTargetSDK
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
    private lateinit var minimumOsPie: ThemePieChart
    private lateinit var targetOsPie: ThemePieChart
    private lateinit var installLocationPie: ThemePieChart
    private lateinit var packageTypePie: ThemePieChart
    private lateinit var minSdkHeading: TypeFaceTextView

    private val analyticsViewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        settings = view.findViewById(R.id.configuration_button)
        search = view.findViewById(R.id.search_button)
        minimumOsPie = view.findViewById(R.id.minimum_os_pie)
        targetOsPie = view.findViewById(R.id.target_os_pie)
        installLocationPie = view.findViewById(R.id.install_location_pie)
        packageTypePie = view.findViewById(R.id.package_type_pie)
        minSdkHeading = view.findViewById(R.id.min_sdk_heading)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            minimumOsPie.gone()
            minSdkHeading.gone()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        analyticsViewModel.getMinimumOsData().observe(viewLifecycleOwner) {
            minimumOsPie.apply {
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
                        openFragmentSlide(AnalyticsMinimumSDK.newInstance(e), "sdk")
                    }
                })
            }

            minimumOsPie.setAnimation(true)
            minimumOsPie.notifyDataSetChanged()
            minimumOsPie.invalidate()
            minimumOsPie.marker = ChartMarkerView(requireContext(), R.layout.marker_view)
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
                        openFragmentSlide(AnalyticsTargetSDK.newInstance(e), "target_sdk")
                    }
                })
            }

            targetOsPie.setAnimation(false)
            targetOsPie.notifyDataSetChanged()
            targetOsPie.invalidate()
            targetOsPie.marker = ChartMarkerView(requireContext(), R.layout.marker_view)
        }

        analyticsViewModel.getInstallLocationData().observe(viewLifecycleOwner) {
            installLocationPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = ColorTemplate.PASTEL_COLORS.toMutableList()
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }
            }

            installLocationPie.setAnimation(false)
            installLocationPie.notifyDataSetChanged()
            installLocationPie.invalidate()
            installLocationPie.marker = ChartMarkerView(requireContext(), R.layout.marker_view)
        }

        analyticsViewModel.getPackageTypeData().observe(viewLifecycleOwner) {
            packageTypePie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = ColorTemplate.PASTEL_COLORS.toMutableList()
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }
            }

            packageTypePie.setAnimation(false)
            packageTypePie.notifyDataSetChanged()
            packageTypePie.invalidate()
            packageTypePie.marker = ChartMarkerView(requireContext(), R.layout.marker_view)
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