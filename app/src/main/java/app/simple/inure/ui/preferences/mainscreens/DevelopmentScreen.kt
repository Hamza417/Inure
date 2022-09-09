package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.CheckBox
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.ui.launcher.Setup

class DevelopmentScreen : ScopedFragment() {

    private lateinit var setup: DynamicRippleRelativeLayout
    private lateinit var textViewXmlViewerSwitchView: CheckBox
    private lateinit var hidePreferenceIndicator: CheckBox
    private lateinit var debugFeaturesSwitch: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_development, container, false)

        setup = view.findViewById(R.id.development_setup)
        textViewXmlViewerSwitchView = view.findViewById(R.id.configuration_use_text_view)
        hidePreferenceIndicator = view.findViewById(R.id.prefs_drawable_switch)
        debugFeaturesSwitch = view.findViewById(R.id.debug_switch)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewXmlViewerSwitchView.setChecked(DevelopmentPreferences.isWebViewXmlViewer())
        hidePreferenceIndicator.setChecked(DevelopmentPreferences.isPreferencesIndicatorHidden())
        debugFeaturesSwitch.setChecked(DevelopmentPreferences.isDebugStateEnabled())

        setup.setOnClickListener {
            openFragmentSlide(Setup.newInstance(), "setup")
        }

        textViewXmlViewerSwitchView.setOnCheckedChangeListener { isChecked ->
            DevelopmentPreferences.setWebViewXmlViewer(isChecked)
        }

        hidePreferenceIndicator.setOnCheckedChangeListener {
            DevelopmentPreferences.setHidePreferencesIndicator(it)
        }

        debugFeaturesSwitch.setOnCheckedChangeListener {
            DevelopmentPreferences.setDebugFeaturesState(it)
        }
    }

    companion object {
        fun newInstance(): DevelopmentScreen {
            val args = Bundle()
            val fragment = DevelopmentScreen()
            fragment.arguments = args
            return fragment
        }
    }
}