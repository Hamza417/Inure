package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.TextViewUtils.makeLinks

class AccessibilityScreen : ScopedFragment() {

    private lateinit var highlight: SwitchView
    private lateinit var stroke: SwitchView
    private lateinit var divider: SwitchView
    private lateinit var reduceAnimations: SwitchView

    private lateinit var strokeContainer: ConstraintLayout
    private lateinit var reduceAnimationsDesc: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_accessibility, container, false)

        highlight = view.findViewById(R.id.highlight_switch)
        stroke = view.findViewById(R.id.highlight_stroke_switch)
        divider = view.findViewById(R.id.list_divider_switch)
        reduceAnimations = view.findViewById(R.id.reduce_animation_switch)

        strokeContainer = view.findViewById(R.id.stroke_container)
        reduceAnimationsDesc = view.findViewById(R.id.reduce_animation_desc)
        reduceAnimationsDesc.text = getString(R.string.desc_reduce_animations, getString(R.string.behaviour))

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        highlight.setChecked(AccessibilityPreferences.isHighlightMode())
        stroke.setChecked(AccessibilityPreferences.isHighlightStroke())
        divider.setChecked(AccessibilityPreferences.isDividerEnabled())
        reduceAnimations.setChecked(AccessibilityPreferences.isAnimationReduced())

        if (highlight.isChecked()) {
            strokeContainer.alpha = 1F
        } else {
            strokeContainer.alpha = 0.4F
        }

        stroke.isEnabled = highlight.isChecked()

        highlight.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setHighlightMode(it)
            stroke.isEnabled = it
            if (it) {
                strokeContainer.animate()
                    .alpha(1F)
                    .setDuration(getInteger(R.integer.animation_duration).toLong())
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .start()
            } else {
                strokeContainer.animate()
                    .alpha(0.4F)
                    .setDuration(getInteger(R.integer.animation_duration).toLong())
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .start()
            }
        }

        stroke.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setHighlightStroke(it)
        }

        divider.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setDivider(it)
        }

        reduceAnimations.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setReduceAnimations(it)
        }

        reduceAnimationsDesc.makeLinks(Pair(getString(R.string.behaviour), object : View.OnClickListener {
            override fun onClick(v: View) {
                clearExitTransition()
                FragmentHelper.openFragment(parentFragmentManager, BehaviourScreen.newInstance(), "behavior_screen")
            }
        }))
    }

    companion object {
        fun newInstance(): AccessibilityScreen {
            val args = Bundle()
            val fragment = AccessibilityScreen()
            fragment.arguments = args
            return fragment
        }
    }
}