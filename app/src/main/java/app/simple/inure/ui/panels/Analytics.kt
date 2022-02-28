package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.TypeFace
import app.simple.inure.viewmodels.panels.AnalyticsViewModel
import com.razerdp.widget.animatedpieview.AnimatedPieView
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig

class Analytics : ScopedFragment() {

    private lateinit var settings: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var minimumOsPie: AnimatedPieView
    private val analyticsViewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        settings = view.findViewById(R.id.configuration_button)
        search = view.findViewById(R.id.search_button)
        minimumOsPie = view.findViewById(R.id.minimum_os_pie)

        startPostponedEnterTransition()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyticsViewModel.getMinimumOsData().observe(viewLifecycleOwner) { animatedPieViewConfig ->
            animatedPieViewConfig.apply {
                animOnTouch(true)
                duration(1000)
                drawText(true)
                interpolator(DecelerateInterpolator(1.5F))
                textGravity(AnimatedPieViewConfig.ECTOPIC)
                autoSize(true)
                textSize(32F)
                pieRadius(250F)
                strokeMode(true)
                strokeWidth(150)
                floatShadowRadius(56f)
                animatePie(true)
                typeFae(TypeFace.getTypeFace(AppearancePreferences.getAppFont(), 2, requireContext()))
                legendsWith(view.findViewById(R.id.min_os_legends) as ViewGroup?)
            }.also {
                minimumOsPie.start(it)
            }
        }

        settings.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        MainPreferencesScreen.newInstance(),
                                        "preferences_screen")
        }

        search.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "preferences_screen")
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