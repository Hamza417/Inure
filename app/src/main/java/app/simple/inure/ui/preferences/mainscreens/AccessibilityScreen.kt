package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterColorPalette
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.appearances.PopupPalettes
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.util.TextViewUtils.makeLinks
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AccessibilityScreen : ScopedFragment() {

    private lateinit var highlight: Switch
    private lateinit var stroke: Switch
    private lateinit var divider: Switch
    private lateinit var reduceAnimations: Switch
    private lateinit var enableContexts: Switch
    private lateinit var colorfulIcons: Switch

    private lateinit var palette: DynamicRippleTextView
    private lateinit var paletteRecyclerView: CustomHorizontalRecyclerView
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
        paletteRecyclerView = view.findViewById(R.id.palette_rv)
        colorfulIcons = view.findViewById(R.id.colorful_icons_switch)
        paletteContainer = view.findViewById(R.id.color_palette_container)

        strokeContainer = view.findViewById(R.id.stroke_container)
        reduceAnimationsDesc = view.findViewById(R.id.reduce_animation_desc)
        reduceAnimationsDesc.text = getString(R.string.desc_reduce_animations, getString(R.string.behavior))

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        highlight.isChecked = AccessibilityPreferences.isHighlightMode()
        stroke.isChecked = AccessibilityPreferences.isHighlightStroke()
        divider.isChecked = AccessibilityPreferences.isDividerEnabled()
        reduceAnimations.isChecked = AccessibilityPreferences.isAnimationReduced()
        enableContexts.isChecked = AccessibilityPreferences.isAppElementsContext()
        colorfulIcons.isChecked = AccessibilityPreferences.isColorfulIcons()
        palette.setPalette()
        paletteRecyclerView.adapter = AdapterColorPalette()

        if (highlight.isChecked) {
            strokeContainer.visible(false)
        } else {
            strokeContainer.gone()
        }

        if (colorfulIcons.isChecked) {
            paletteContainer.visibility = View.VISIBLE
        } else {
            paletteContainer.visibility = View.GONE
        }

        stroke.isEnabled = highlight.isChecked
        paletteContainer.isEnabled = colorfulIcons.isChecked

        highlight.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setHighlightMode(it)
            stroke.isEnabled = it
            if (it) {
                strokeContainer.visible(false)
            } else {
                strokeContainer.gone()
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

        reduceAnimationsDesc.makeLinks(Pair(getString(R.string.behavior), object : View.OnClickListener {
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
            AccessibilityPreferences.COLORFUL_ICONS_PALETTE -> {
                palette.setPalette()
                paletteRecyclerView.animate()
                    .alpha(0F)
                    .setDuration(getInteger(R.integer.animation_duration).toLong())
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .withEndAction {
                        paletteRecyclerView.adapter = AdapterColorPalette()
                        paletteRecyclerView.animate()
                            .alpha(1F)
                            .setDuration(getInteger(R.integer.animation_duration).toLong())
                            .setInterpolator(DecelerateInterpolator(1.5F))
                            .start()
                    }
                    .start()
            }

            AccessibilityPreferences.BOTTOM_MENU_CONTEXT -> {
                /**
                 * Reset the bottom menu height so that it can be reinitialized
                 * with the updated height later in [app.simple.inure.decorations.views.BottomMenuRecyclerView].
                 *
                 * This is done because the height of the bottom menu is calculated
                 * based on the height of the bottom menu items. So, if the height of the
                 * bottom menu items is changed, the height of the bottom menu should be
                 * recalculated. For some reason the height of the bottom menu items is not
                 * recalculated when the height of the bottom menu is changed.
                 *
                 * This is a workaround for that.
                 */
                BottomMenuConstants.setBottomMenuHeight(0)
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
