package app.simple.inure.ui.preferences.mainscreens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.appearance.IconSize
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupAppTheme
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.interfaces.ThemeRevealCoordinatesListener
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.TextViewUtils.makeLinks
import app.simple.inure.util.ThemeUtils

class AppearanceScreen : ScopedFragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var roundedCorner: DynamicRippleRelativeLayout
    private lateinit var iconSize: DynamicRippleRelativeLayout
    private lateinit var appTheme: DynamicRippleTextView
    private lateinit var iconShadows: SwitchView
    private lateinit var coloredIconShadows: SwitchView
    private lateinit var accentOnNav: SwitchView
    private lateinit var transparentStatus: SwitchView

    private lateinit var descTransparentStatus: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)
        iconSize = view.findViewById(R.id.appearance_icon_size)

        appTheme = view.findViewById(R.id.popup_application_theme)

        iconShadows = view.findViewById(R.id.appearance_icons_shadow_switch)
        coloredIconShadows = view.findViewById(R.id.colored_icons_switch)
        accentOnNav = view.findViewById(R.id.appearance_nav_color_switch)
        transparentStatus = view.findViewById(R.id.appearance_transparent_status_switch)

        descTransparentStatus = view.findViewById(R.id.transparent_status_desc)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAppThemeText()
        iconShadows.setChecked(AppearancePreferences.isIconShadowsOn())
        coloredIconShadows.setChecked(AppearancePreferences.getColoredIconShadows())
        accentOnNav.setChecked(AppearancePreferences.isAccentOnNavigationBar())
        transparentStatus.setChecked(AppearancePreferences.isTransparentStatusDisabled())

        appTheme.setOnClickListener {
            PopupAppTheme(it)
        }

        appTheme.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val location = IntArray(2)
                appTheme.getLocationOnScreen(location)
                val x = location[0] + appTheme.measuredWidth / 2
                val y = location[1] + appTheme.measuredHeight / 2

                if (AppearancePreferences.isTransparentStatusDisabled()) {
                    (requireActivity() as ThemeRevealCoordinatesListener).onTouchCoordinates(
                            event.rawX,
                            event.rawY - StatusBarHeight.getStatusBarHeight(resources))
                } else {
                    (requireActivity() as ThemeRevealCoordinatesListener).onTouchCoordinates(x.toFloat(), y.toFloat())
                }
            }
            false
        }

        accent.setOnClickListener {
            FragmentHelper.openFragment(parentFragmentManager, AccentColor.newInstance(), "accent_color")
        }

        typeface.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, AppearanceTypeFace.newInstance(), "typeface")
        }

        roundedCorner.setOnClickListener {
            RoundedCorner.newInstance().show(childFragmentManager, "rounded_corner")
        }

        iconSize.setOnClickListener {
            IconSize.newInstance()
                .show(childFragmentManager, "icon_size")
        }

        iconShadows.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setIconShadows(it)
        }

        coloredIconShadows.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setColoredIconShadowsState(it)
        }

        accentOnNav.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setAccentOnNavigationBar(it)
        }

        transparentStatus.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setTransparentStatusState(it)
        }

        descTransparentStatus.makeLinks(Pair("AndroidBug #5497", View.OnClickListener {
            val uri: Uri = Uri.parse("https://issuetracker.google.com/issues/36911528")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }))
    }

    private fun setAppThemeText() {
        appTheme.text = when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME -> {
                getString(R.string.light)
            }
            ThemeConstants.DARK_THEME -> {
                getString(R.string.dark)
            }
            ThemeConstants.FOLLOW_SYSTEM -> {
                getString(R.string.follow_system)
            }
            ThemeConstants.AMOLED -> {
                "AMOLED"
            }
            ThemeConstants.DAY_NIGHT -> {
                getString(R.string.day_night)
            }
            ThemeConstants.SLATE -> {
                getString(R.string.slate)
            }
            ThemeConstants.HIGH_CONTRAST -> {
                getString(R.string.high_contrast)
            }
            else -> {
                getString(R.string.unknown)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.theme -> {
                setAppThemeText()
                handler.postDelayed({ ThemeUtils.setAppTheme(resources) }, 100)
            }
            AppearancePreferences.accentOnNav -> {
                if (AppearancePreferences.isAccentOnNavigationBar()) {
                    requireActivity().window.navigationBarColor = requireContext().resolveAttrColor(R.attr.colorAppAccent)
                } else {
                    requireActivity().recreate()
                }
            }
            AppearancePreferences.transparentStatus -> {
                requireActivity().recreate()
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
