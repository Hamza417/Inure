package app.simple.inure.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.decorations.animatedbackground.DynamicRippleRelativeLayout
import app.simple.inure.decorations.switch.SwitchCallbacks
import app.simple.inure.decorations.switch.SwitchView
import app.simple.inure.dialogs.appearance.AccentColor
import app.simple.inure.dialogs.appearance.AppearanceTypeFace
import app.simple.inure.dialogs.appearance.RoundedCorner
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ViewUtils

class AppearanceScreen : Fragment() {

    private lateinit var accent: DynamicRippleRelativeLayout
    private lateinit var typeface: DynamicRippleRelativeLayout
    private lateinit var roundedCorner: DynamicRippleRelativeLayout

    private lateinit var dimWindows: SwitchView
    private lateinit var shadows: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_appearances, container, false)

        accent = view.findViewById(R.id.appearance_accent_color)
        typeface = view.findViewById(R.id.appearance_app_typeface)
        roundedCorner = view.findViewById(R.id.appearance_corner_radius)

        dimWindows = view.findViewById(R.id.appearance_switch_dim_windows)
        shadows = view.findViewById(R.id.appearance_switch_shadows)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dimWindows.setChecked(AppearancePreferences.isDimmingOn())
        shadows.setChecked(AppearancePreferences.areShadowsOn())

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