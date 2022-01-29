package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.BehaviourPreferences

class BehaviourScreen : ScopedFragment() {

    private lateinit var dimWindows: SwitchView
    private lateinit var shadows: SwitchView
    private lateinit var transition: SwitchView
    private lateinit var animations: SwitchView
    private lateinit var marquee: SwitchView
    private lateinit var skipLoading: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_behaviour, container, false)

        dimWindows = view.findViewById(R.id.appearance_switch_dim_windows)
        shadows = view.findViewById(R.id.appearance_switch_shadows)
        transition = view.findViewById(R.id.appearance_transition_switch)
        animations = view.findViewById(R.id.appearance_animations_switch)
        marquee = view.findViewById(R.id.appearance_marquee_switch)
        skipLoading = view.findViewById(R.id.skip_loading_switch)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dimWindows.setChecked(BehaviourPreferences.isDimmingOn())
        shadows.setChecked(BehaviourPreferences.areShadowsOn())
        transition.setChecked(BehaviourPreferences.isTransitionOn())
        animations.setChecked(BehaviourPreferences.isAnimationOn())
        marquee.setChecked(BehaviourPreferences.isMarqueeOn())
        skipLoading.setChecked(BehaviourPreferences.isSkipLoadingMainScreenState())

        dimWindows.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setDimWindows(it)
        }

        shadows.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setShadows(it)
        }

        transition.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setTransitionOn(it)
        }

        animations.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setAnimations(it)
        }

        marquee.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setMarquee(it)
        }

        skipLoading.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setSkipLoadingMainScreenState(it)
        }
    }

    companion object {
        fun newInstance(): BehaviourScreen {
            val args = Bundle()
            val fragment = BehaviourScreen()
            fragment.arguments = args
            return fragment
        }
    }
}