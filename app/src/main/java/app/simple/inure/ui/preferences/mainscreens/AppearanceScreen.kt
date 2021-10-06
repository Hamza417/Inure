package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.dialogs.appearance.AccentColor
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupAppTheme
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ThemeSetter

class AppearanceScreen : ScopedFragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var roundedCorner: DynamicRippleRelativeLayout
    private lateinit var appTheme: DynamicRippleTextView
    private lateinit var iconShadows: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)

        appTheme = view.findViewById(R.id.popup_application_theme)

        iconShadows = view.findViewById(R.id.appearance_icons_shadow_switch)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAppThemeText()
        iconShadows.setChecked(AppearancePreferences.isIconShadowsOn())

        appTheme.setOnClickListener {
            PopupAppTheme(it)
        }

        accent.setOnClickListener {
            AccentColor.newInstance().show(childFragmentManager, "accent_color")
        }

        typeface.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, AppearanceTypeFace.newInstance(), "typeface")
        }

        roundedCorner.setOnClickListener {
            RoundedCorner.newInstance().show(childFragmentManager, "rounded_corner")
        }

        iconShadows.setOnSwitchCheckedChangeListener {
            AppearancePreferences.setIconShadows(it)
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
