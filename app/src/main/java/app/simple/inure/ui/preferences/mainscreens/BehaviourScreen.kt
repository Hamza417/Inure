package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupDampingRatio
import app.simple.inure.popups.behavior.PopupStiffness
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.BehaviourPreferences

class BehaviourScreen : ScopedFragment() {

    private lateinit var dimWindows: SwitchView
    private lateinit var blurWindows: SwitchView
    private lateinit var shadows: SwitchView
    private lateinit var transition: SwitchView
    private lateinit var animations: SwitchView
    private lateinit var marquee: SwitchView
    private lateinit var skipLoading: SwitchView

    private lateinit var transitionType: DynamicRippleTextView
    private lateinit var arcType: DynamicRippleTextView
    private lateinit var dampingRatio: DynamicRippleTextView
    private lateinit var stiffness: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_behaviour, container, false)

        dimWindows = view.findViewById(R.id.appearance_switch_dim_windows)
        blurWindows = view.findViewById(R.id.appearance_switch_blur_windows)
        shadows = view.findViewById(R.id.appearance_switch_shadows)
        transition = view.findViewById(R.id.appearance_transition_switch)
        animations = view.findViewById(R.id.appearance_animations_switch)
        marquee = view.findViewById(R.id.appearance_marquee_switch)
        skipLoading = view.findViewById(R.id.skip_loading_switch)

        transitionType = view.findViewById(R.id.popup_transition_type)
        arcType = view.findViewById(R.id.popup_arc_transition_type)
        dampingRatio = view.findViewById(R.id.popup_damping_ratio)
        stiffness = view.findViewById(R.id.popup_stiffness)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dimWindows.setChecked(BehaviourPreferences.isDimmingOn())
        blurWindows.setChecked(BehaviourPreferences.isBlurringOn())
        shadows.setChecked(BehaviourPreferences.areColoredShadowsOn())
        transition.setChecked(BehaviourPreferences.isTransitionOn())
        animations.setChecked(BehaviourPreferences.isArcAnimationOn())
        marquee.setChecked(BehaviourPreferences.isMarqueeOn())
        skipLoading.setChecked(BehaviourPreferences.isSkipLoadingMainScreenState())

        setTransitionType()
        setArcType()
        setDampingRatio()
        setStiffness()

        dimWindows.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setDimWindows(it)
        }

        blurWindows.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setBlurWindows(it)
        }

        shadows.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setColoredShadows(it)
        }

        transition.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setTransitionOn(it)
        }

        animations.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setArcAnimations(it)
        }

        marquee.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setMarquee(it)
        }

        transitionType.setOnClickListener {
            PopupTransitionType(it)
        }

        arcType.setOnClickListener {
            PopupArcType(it)
        }

        dampingRatio.setOnClickListener {
            PopupDampingRatio(it)
        }

        stiffness.setOnClickListener {
            PopupStiffness(it)
        }

        skipLoading.setOnSwitchCheckedChangeListener {
            BehaviourPreferences.setSkipLoadingMainScreenState(it)
        }
    }

    private fun setTransitionType() {
        transitionType.text = when (BehaviourPreferences.getTransitionType()) {
            PopupTransitionType.FADE -> getString(R.string.fade)
            PopupTransitionType.ELEVATION -> getString(R.string.elevation)
            PopupTransitionType.SHARED_AXIS_X -> getString(R.string.shared_axis, "X")
            PopupTransitionType.SHARED_AXIS_Y -> getString(R.string.shared_axis, "Y")
            PopupTransitionType.SHARED_AXIS_Z -> getString(R.string.shared_axis, "Z")
            PopupTransitionType.THROUGH -> getString(R.string.through)
            else -> getString(R.string.unknown)
        }
    }

    private fun setArcType() {
        arcType.text = when (BehaviourPreferences.getArcType()) {
            PopupArcType.INURE -> getString(R.string.app_name)
            PopupArcType.MATERIAL -> getString(R.string.material)
            PopupArcType.LEGACY -> getString(R.string.legacy)
            else -> getString(R.string.unknown)
        }
    }

    private fun setDampingRatio() {
        dampingRatio.text = when (BehaviourPreferences.getDampingRatio()) {
            SpringForce.DAMPING_RATIO_NO_BOUNCY -> getString(R.string.none)
            SpringForce.DAMPING_RATIO_LOW_BOUNCY -> getString(R.string.low)
            SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY -> getString(R.string.medium)
            SpringForce.DAMPING_RATIO_HIGH_BOUNCY -> getString(R.string.high)
            else -> getString(R.string.unknown)
        }
    }

    private fun setStiffness() {
        stiffness.text = when (BehaviourPreferences.getStiffness()) {
            SpringForce.STIFFNESS_VERY_LOW -> getString(R.string.very_low)
            SpringForce.STIFFNESS_LOW -> getString(R.string.low)
            SpringForce.STIFFNESS_MEDIUM -> getString(R.string.medium)
            SpringForce.STIFFNESS_HIGH -> getString(R.string.high)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BehaviourPreferences.dampingRatio -> {
                setDampingRatio()
            }
            BehaviourPreferences.stiffness -> {
                setStiffness()
            }
            BehaviourPreferences.transitionType -> {
                setTransitionType()
            }
            BehaviourPreferences.arcType -> {
                setArcType()
            }
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