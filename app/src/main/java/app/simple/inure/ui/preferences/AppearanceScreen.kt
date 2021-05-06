package app.simple.inure.ui.preferences

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchCallbacks
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.dialogs.appearance.AccentColor
import app.simple.inure.dialogs.appearance.AppearanceTypeFace
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupAppTheme
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ThemeSetter

class AppearanceScreen : ScopedFragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var roundedCorner: DynamicRippleRelativeLayout

    private lateinit var appTheme: DynamicRippleTextView

    private lateinit var dimWindows: SwitchView
    private lateinit var shadows: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)

        appTheme = view.findViewById(R.id.popup_application_theme)

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

        appTheme.setOnClickListener {
            PopupAppTheme(layoutInflater.inflate(R.layout.popup_application_theme,
                                                 PopupLinearLayout(requireContext()),
                                                 true), it)
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

        dimWindows.setOnSwitchCheckedChangeListener { isChecked -> AppearancePreferences.setDimWindows(isChecked) }

        shadows.setOnSwitchCheckedChangeListener { isChecked -> AppearancePreferences.setShadows(isChecked) }
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
