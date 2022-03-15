package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.TextViewUtils.makeLinks

class AccessibilityScreen : ScopedFragment() {

    private lateinit var highlight: SwitchView
    private lateinit var divider: SwitchView
    private lateinit var reduceAnimations: SwitchView

    private lateinit var reduceAnimationsDesc: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_accessibility, container, false)

        highlight = view.findViewById(R.id.highlight_switch)
        divider = view.findViewById(R.id.list_divider_switch)
        reduceAnimations = view.findViewById(R.id.reduce_animation_switch)

        reduceAnimationsDesc = view.findViewById(R.id.reduce_animation_desc)
        reduceAnimationsDesc.setText(getString(R.string.desc_reduce_animations, getString(R.string.behaviour)))

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        highlight.setChecked(AccessibilityPreferences.isHighlightMode())
        divider.setChecked(AccessibilityPreferences.isDividerEnabled())
        reduceAnimations.setChecked(AccessibilityPreferences.isAnimationReduced())

        highlight.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setHighlightMode(it)
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