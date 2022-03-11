package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.menus.AnalyticsMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.AnalyticsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet

class Analytics : ScopedFragment() {

    private lateinit var settings: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var minimumOsPie: PieChart
    private lateinit var targetOsPie: PieChart
    private val analyticsViewModel: AnalyticsViewModel by viewModels()

    private val chartOffset = 20F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        settings = view.findViewById(R.id.configuration_button)
        search = view.findViewById(R.id.search_button)
        minimumOsPie = view.findViewById(R.id.minimum_os_pie)
        targetOsPie = view.findViewById(R.id.target_os_pie)

        startPostponedEnterTransition()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyticsViewModel.getMinimumOsData().observe(viewLifecycleOwner) {
            minimumOsPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = it.second
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }

                legend.apply {
                    isEnabled = true
                    formSize = 10F
                    formToTextSpace = 5F
                    form = Legend.LegendForm.DEFAULT
                    textColor = ThemeManager.theme.textViewTheme.secondaryTextColor
                    this.xEntrySpace = 20F
                    this.yEntrySpace = 5F
                    isWordWrapEnabled = true
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                }

                holeRadius = 50F
                setUsePercentValues(false)
                dragDecelerationFrictionCoef = 0.95f
                isHighlightPerTapEnabled = true
                description.isEnabled = false
                setExtraOffsets(chartOffset, chartOffset, chartOffset, chartOffset)
                setDrawCenterText(false)
                animateXY(1000, 500, Easing.EaseOutCubic)
            }

            minimumOsPie.notifyDataSetChanged()
            minimumOsPie.invalidate()
        }

        analyticsViewModel.getTargetOsData().observe(viewLifecycleOwner) {
            targetOsPie.apply {
                PieDataSet(it.first, "").apply {
                    data = PieData(this)
                    colors = it.second
                    valueTextColor = Color.TRANSPARENT
                    setEntryLabelColor(Color.TRANSPARENT)
                }

                legend.apply {
                    isEnabled = true
                    formSize = 10F
                    formToTextSpace = 5F
                    form = Legend.LegendForm.DEFAULT
                    textColor = ThemeManager.theme.textViewTheme.secondaryTextColor
                    this.xEntrySpace = 20F
                    this.yEntrySpace = 5F
                    isWordWrapEnabled = true
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                }

                holeRadius = 50F
                setUsePercentValues(false)
                dragDecelerationFrictionCoef = 0.95f
                isHighlightPerTapEnabled = true
                description.isEnabled = false
                setExtraOffsets(chartOffset, chartOffset, chartOffset, chartOffset)
                setDrawCenterText(false)
                animateXY(1000, 500, Easing.EaseOutCubic)
            }

            targetOsPie.notifyDataSetChanged()
            targetOsPie.invalidate()
        }

        settings.setOnClickListener {
            AnalyticsMenu.newInstance()
                .show(childFragmentManager, "analytics_menu")
        }

        search.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "preferences_screen")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AnalyticsPreferences.sdkValue -> {
                analyticsViewModel.refresh()
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