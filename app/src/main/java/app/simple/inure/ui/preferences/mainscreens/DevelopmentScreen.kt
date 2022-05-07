package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.ui.launcher.Setup
import app.simple.inure.util.FragmentHelper

class DevelopmentScreen : ScopedFragment() {

    private lateinit var setup: DynamicRippleRelativeLayout
    private lateinit var textViewXmlViewerSwitchView: SwitchView
    private lateinit var fullScreenAudio: SwitchView
    private lateinit var hidePreferenceIndicator: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_development, container, false)

        setup = view.findViewById(R.id.development_setup)
        textViewXmlViewerSwitchView = view.findViewById(R.id.configuration_use_text_view)
        fullScreenAudio = view.findViewById(R.id.full_screen_player)
        hidePreferenceIndicator = view.findViewById(R.id.prefs_drawable_switch)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewXmlViewerSwitchView.setChecked(DevelopmentPreferences.isWebViewXmlViewer())
        fullScreenAudio.setChecked(DevelopmentPreferences.isAudioPlayerFullScreen())
        hidePreferenceIndicator.setChecked(DevelopmentPreferences.isPreferencesIndicatorHidden())

        setup.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, Setup.newInstance(), "setup")
        }

        textViewXmlViewerSwitchView.setOnSwitchCheckedChangeListener { isChecked ->
            DevelopmentPreferences.setWebViewXmlViewer(isChecked)
        }

        fullScreenAudio.setOnSwitchCheckedChangeListener {
            DevelopmentPreferences.setFullScreenAudioPlayer(it)
        }

        hidePreferenceIndicator.setOnSwitchCheckedChangeListener {
            DevelopmentPreferences.setHidePreferencesIndicator(it)
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