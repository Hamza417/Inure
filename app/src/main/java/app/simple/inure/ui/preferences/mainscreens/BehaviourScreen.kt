package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.behavior.DampingRatio.Companion.showDampingRatioDialog
import app.simple.inure.dialogs.behavior.Stiffness.Companion.showStiffnessDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.behavior.PopupArcType
import app.simple.inure.popups.behavior.PopupDampingRatio
import app.simple.inure.popups.behavior.PopupStiffness
import app.simple.inure.popups.behavior.PopupTransitionType
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.DevelopmentPreferences

class BehaviourScreen : ScopedFragment() {

    private lateinit var dimWindows: Switch
    private lateinit var blurWindows: Switch
    private lateinit var shadows: Switch
    private lateinit var transition: Switch
    private lateinit var animations: Switch
    private lateinit var marquee: Switch
    private lateinit var skipLoading: Switch

    private lateinit var transitionType: DynamicRippleTextView
    private lateinit var arcType: DynamicRippleTextView
    private lateinit var dampingRatio: DynamicRippleConstraintLayout
    private lateinit var stiffness: DynamicRippleConstraintLayout

    private lateinit var blurWindowsContainer: ConstraintLayout

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
        dampingRatio = view.findViewById(R.id.damping_ratio_container)
        stiffness = view.findViewById(R.id.stiffness_container)

        blurWindowsContainer = view.findViewById(R.id.blur_windows_container)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dimWindows.isChecked = BehaviourPreferences.isDimmingOn()
        blurWindows.isChecked = BehaviourPreferences.isBlurringOn()
        shadows.isChecked = BehaviourPreferences.isColoredShadow()
        transition.isChecked = BehaviourPreferences.isTransitionOn()
        animations.isChecked = BehaviourPreferences.isArcAnimationOn()
        marquee.isChecked = BehaviourPreferences.isMarqueeOn()
        skipLoading.isChecked = BehaviourPreferences.isSkipLoading()

        setTransitionType()
        setArcType()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (requireActivity().windowManager.isCrossWindowBlurEnabled) {
                blurWindowsContainer.visibility = View.VISIBLE
            } else {
                blurWindowsContainer.visibility = View.GONE
                blurWindows.isChecked = false
                BehaviourPreferences.setBlurWindows(false)
            }
        }

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
            if (fullVersionCheck(goBack = false)) {
                PopupTransitionType(it)
            }
        }

        arcType.setOnClickListener {
            if (fullVersionCheck(goBack = false)) {
                PopupArcType(it)
            }
        }

        dampingRatio.setOnClickListener {
            if (DevelopmentPreferences.get(DevelopmentPreferences.oldStyleScrollingBehaviorDialog)) {
                PopupDampingRatio(view)
            } else {
                childFragmentManager.showDampingRatioDialog()
            }
        }

        stiffness.setOnClickListener {
            if (DevelopmentPreferences.get(DevelopmentPreferences.oldStyleScrollingBehaviorDialog)) {
                PopupStiffness(view)
            } else {
                childFragmentManager.showStiffnessDialog()
            }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BehaviourPreferences.DAMPING_RATIO -> {
                // setDampingRatio()
            }
            BehaviourPreferences.STIFFNESS -> {
                // setStiffness()
            }
            BehaviourPreferences.TRANSITION_TYPE -> {
                setTransitionType()
            }
            BehaviourPreferences.ARC_TYPE -> {
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
