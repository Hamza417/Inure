package app.simple.inure.ui.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.dynamicanimation.animation.SpringForce
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switch.SwitchCallbacks
import app.simple.inure.decorations.switch.SwitchView
import app.simple.inure.dialogs.appearance.AccentColor
import app.simple.inure.dialogs.appearance.AppearanceTypeFace
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupAppTheme
import app.simple.inure.popups.app.PopupBounce
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ThemeSetter

class AppearanceScreen : ScopedFragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var roundedCorner: DynamicRippleRelativeLayout

    private lateinit var appTheme: DynamicRippleTextView
    private lateinit var scrollBounce: DynamicRippleTextView

    private lateinit var dimWindows: SwitchView
    private lateinit var shadows: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)

        appTheme = view.findViewById(R.id.popup_application_theme)
        scrollBounce = view.findViewById(R.id.popup_bouncy_value)

        dimWindows = view.findViewById(R.id.appearance_switch_dim_windows)
        shadows = view.findViewById(R.id.appearance_switch_shadows)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dimWindows.setChecked(AppearancePreferences.isDimmingOn())
        shadows.setChecked(AppearancePreferences.areShadowsOn())
        setAppThemeText()
        onSharedPreferenceChanged(null, AppearancePreferences.bounce)

        appTheme.setOnClickListener {
            PopupAppTheme(layoutInflater.inflate(R.layout.popup_application_theme,
                                                 DynamicCornerLinearLayout(requireContext(), null),
                                                 true),
                          it)
        }

        accent.setOnClickListener {
            AccentColor.newInstance().show(childFragmentManager, "accent_color")
        }

        typeface.setOnClickListener {
            AppearanceTypeFace.newInstance().show(childFragmentManager, "appearance_type_face")
        }

        roundedCorner.setOnClickListener {
            RoundedCorner.newInstance().show(childFragmentManager, "rounded_corner")
        }

        dimWindows.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                AppearancePreferences.setDimWindows(isChecked)
            }
        })

        shadows.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                AppearancePreferences.setShadows(isChecked)
            }
        })

        scrollBounce.setOnClickListener {
            PopupBounce(layoutInflater.inflate(R.layout.popup_bounce,
                                               DynamicCornerLinearLayout(requireContext(), null),
                                               true), it)
        }
    }

    private fun setAppThemeText() {
        appTheme.text = when (AppearancePreferences.getAppTheme()) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                getString(R.string.light)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                getString(R.string.dark)
            }
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                getString(R.string.follow_system)
            }
            else -> {
                getString(R.string.unknown)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.appTheme -> {
                setAppThemeText()
                ThemeSetter.setAppTheme(AppearancePreferences.getAppTheme())
            }
            AppearancePreferences.bounce -> {
                scrollBounce.text = when (AppearancePreferences.getScrollBounce()) {
                    SpringForce.DAMPING_RATIO_NO_BOUNCY -> getString(R.string.none)
                    SpringForce.DAMPING_RATIO_LOW_BOUNCY -> getString(R.string.low)
                    SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY -> getString(R.string.medium)
                    0.4F -> getString(R.string.high)
                    else -> getString(R.string.unknown)
                }
            }
        }
    }

    companion object {
        fun newInstance(): AppearanceScreen {
            val args = Bundle()
            val fragment = AppearanceScreen()
            fragment.arguments = args
            return fragment
        }
    }
}
