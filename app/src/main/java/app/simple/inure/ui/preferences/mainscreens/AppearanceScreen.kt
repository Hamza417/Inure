package app.simple.inure.ui.preferences.mainscreens

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.appearance.IconSize
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.appearances.PopupLayoutStyle
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceAppTheme
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace

class AppearanceScreen : ScopedFragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var listStyle: DynamicRippleTextView
    private lateinit var roundedCorner: DynamicRippleRelativeLayout
    private lateinit var iconSize: DynamicRippleRelativeLayout
    private lateinit var appTheme: DynamicRippleRelativeLayout
    private lateinit var iconShadows: Switch
    private lateinit var coloredIconShadows: Switch
    private lateinit var accentOnBottomMenu: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        listStyle = view.findViewById(R.id.popup_list_style)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)
        iconSize = view.findViewById(R.id.appearance_icon_size)
        appTheme = view.findViewById(R.id.appearance_app_theme)
        iconShadows = view.findViewById(R.id.appearance_icons_shadow_switch)
        coloredIconShadows = view.findViewById(R.id.colored_icons_switch)
        accentOnBottomMenu = view.findViewById(R.id.accent_on_bottom_menu_switch)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconShadows.isChecked = AppearancePreferences.isIconShadowsOn()
        coloredIconShadows.isChecked = AppearancePreferences.getColoredIconShadows()
        accentOnBottomMenu.isChecked = AppearancePreferences.isAccentColorOnBottomMenu()
        setLayoutStyle()

        appTheme.setOnClickListener {
            openFragmentSlide(AppearanceAppTheme.newInstance(), AppearanceAppTheme.TAG)
        }

        accent.setOnClickListener {
            openFragmentSlide(AccentColor.newInstance(), AccentColor.TAG)
        }

        typeface.setOnClickListener {
            openFragmentSlide(AppearanceTypeFace.newInstance(), AppearanceTypeFace.TAG)
        }

        listStyle.setOnClickListener {
            if (fullVersionCheck(goBack = false)) {
                PopupLayoutStyle(it)
            }
        }

        roundedCorner.setOnClickListener {
            if (fullVersionCheck(goBack = false)) {
                RoundedCorner.newInstance().show(childFragmentManager, RoundedCorner.TAG)
            }
        }

        iconSize.setOnClickListener {
            if (fullVersionCheck(goBack = false)) {
                IconSize.newInstance()
                    .show(childFragmentManager, IconSize.TAG)
            }
        }

        iconShadows.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setIconShadows(it)
        }

        coloredIconShadows.setOnSwitchCheckedChangeListener {
            if (fullVersionCheck(goBack = false)) {
                AppearancePreferences.setColoredIconShadowsState(it)
            }
        }

        accentOnBottomMenu.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setAccentColorOnBottomMenu(it)
        }
    }

    private fun setLayoutStyle() {
        listStyle.text = when (AppearancePreferences.getListStyle()) {
            AppearancePreferences.LIST_STYLE_NORMAL -> getString(R.string.normal)
            AppearancePreferences.LIST_STYLE_CONDENSED -> getString(R.string.condensed)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AppearancePreferences.LIST_STYLE -> {
                setLayoutStyle()
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

        const val TAG = "appearance_preferences"
    }
}
