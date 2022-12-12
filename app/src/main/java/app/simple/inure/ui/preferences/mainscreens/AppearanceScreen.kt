package app.simple.inure.ui.preferences.mainscreens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.dialogs.appearance.IconSize
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceAppTheme
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace

class AppearanceScreen : ScopedFragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var roundedCorner: DynamicRippleRelativeLayout
    private lateinit var iconSize: DynamicRippleRelativeLayout
    private lateinit var appTheme: DynamicRippleRelativeLayout
    private lateinit var iconShadows: SwitchView
    private lateinit var coloredIconShadows: SwitchView
    private lateinit var accentOnNav: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)
        iconSize = view.findViewById(R.id.appearance_icon_size)

        appTheme = view.findViewById(R.id.appearance_app_theme)

        iconShadows = view.findViewById(R.id.appearance_icons_shadow_switch)
        coloredIconShadows = view.findViewById(R.id.colored_icons_switch)
        accentOnNav = view.findViewById(R.id.appearance_nav_color_switch)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconShadows.setChecked(AppearancePreferences.isIconShadowsOn())
        coloredIconShadows.setChecked(AppearancePreferences.getColoredIconShadows())
        accentOnNav.setChecked(AppearancePreferences.isAccentOnNavigationBar())

        appTheme.setOnClickListener {
            openFragmentSlide(AppearanceAppTheme.newInstance(), "theme")
        }

        accent.setOnClickListener {
            openFragmentSlide(AccentColor.newInstance(), "accent_color")
        }

        typeface.setOnClickListener {
            openFragmentSlide(AppearanceTypeFace.newInstance(), "typeface")
        }

        roundedCorner.setOnClickListener {
            if (fullVersionCheck()) {
                RoundedCorner.newInstance().show(childFragmentManager, "rounded_corner")
            }
        }

        iconSize.setOnClickListener {
            if (fullVersionCheck()) {
                IconSize.newInstance()
                    .show(childFragmentManager, "icon_size")
            }
        }

        iconShadows.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setIconShadows(it)
        }

        coloredIconShadows.setOnSwitchCheckedChangeListener {
            if (fullVersionCheck()) {
                AppearancePreferences.setColoredIconShadowsState(it)
            }
        }

        accentOnNav.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setAccentOnNavigationBar(it)
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
