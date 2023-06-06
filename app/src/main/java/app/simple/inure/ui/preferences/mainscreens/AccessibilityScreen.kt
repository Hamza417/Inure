package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.appearances.PopupPalettes
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.util.TextViewUtils.makeLinks

class AccessibilityScreen : ScopedFragment() {

    private lateinit var highlight: SwitchView
    private lateinit var stroke: SwitchView
    private lateinit var divider: SwitchView
    private lateinit var reduceAnimations: SwitchView
    private lateinit var enableContexts: SwitchView
    private lateinit var colorfulIcons: SwitchView

    private lateinit var palette: DynamicRippleTextView
    private lateinit var paletteContainer: ConstraintLayout
    private lateinit var strokeContainer: ConstraintLayout
    private lateinit var reduceAnimationsDesc: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_accessibility, container, false)

        highlight = view.findViewById(R.id.highlight_switch)
        stroke = view.findViewById(R.id.highlight_stroke_switch)
        divider = view.findViewById(R.id.list_divider_switch)
        reduceAnimations = view.findViewById(R.id.reduce_animation_switch)
        enableContexts = view.findViewById(R.id.enable_context_switch)
        palette = view.findViewById(R.id.color_palette_popup)
        colorfulIcons = view.findViewById(R.id.colorful_icons_switch)
        paletteContainer = view.findViewById(R.id.color_palette_container)

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
        enableContexts.setChecked(AccessibilityPreferences.isAppElementsContext())
        colorfulIcons.setChecked(AccessibilityPreferences.isColorfulIcons())
        palette.setPalette()

        if (highlight.isChecked()) {
            strokeContainer.alpha = 1F
        } else {
            strokeContainer.alpha = 0.4F
        }

        if (colorfulIcons.isChecked()) {
            paletteContainer.visibility = View.VISIBLE
        } else {
            paletteContainer.visibility = View.GONE
        }

        stroke.isEnabled = highlight.isChecked()
        paletteContainer.isEnabled = colorfulIcons.isChecked()

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

        colorfulIcons.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setColorfulIcons(it)
            paletteContainer.isEnabled = it
            if (it) {
                paletteContainer.animate()
                    .alpha(1F)
                    .setDuration(getInteger(R.integer.animation_duration).toLong())
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .start()
            } else {
                paletteContainer.animate()
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
            if (fullVersionCheck(goBack = false)) {
                AccessibilityPreferences.setDivider(it)
            }
        }

        reduceAnimations.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setReduceAnimations(it)
        }

        enableContexts.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setAppElementsContext(it)
        }

        reduceAnimationsDesc.makeLinks(Pair(getString(R.string.behaviour), object : View.OnClickListener {
            override fun onClick(v: View) {
                openFragmentSlide(BehaviourScreen.newInstance(), "behavior_screen")
            }
        }))

        colorfulIcons.setOnSwitchCheckedChangeListener {
            if (fullVersionCheck(goBack = false)) {
                AccessibilityPreferences.setColorfulIcons(it)
                paletteContainer.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        palette.setOnClickListener {
            if (fullVersionCheck(goBack = false)) {
                PopupPalettes(it)
            }
        }
    }

    private fun DynamicRippleTextView.setPalette() {
        text = when (AccessibilityPreferences.getColorfulIconsPalette()) {
            Colors.PASTEL -> getString(R.string.pastel)
            Colors.RETRO -> getString(R.string.retro)
            Colors.COFFEE -> getString(R.string.coffee)
            Colors.COLD -> getString(R.string.cold)
            else -> {
                AccessibilityPreferences.setColorfulIconsPalette(Colors.PASTEL)
                getString(R.string.pastel)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AccessibilityPreferences.colorfulIconsPalette -> {
                palette.setPalette()
            }
        }
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