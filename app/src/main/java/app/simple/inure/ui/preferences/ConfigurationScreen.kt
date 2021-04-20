package app.simple.inure.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import app.simple.inure.R
import app.simple.inure.decorations.switch.SwitchCallbacks
import app.simple.inure.decorations.switch.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences

class ConfigurationScreen : ScopedFragment() {

    private lateinit var keepScreenOnSwitchView: SwitchView
    private lateinit var permissionLabelModeSwitchView: SwitchView
    private lateinit var textViewXmlViewerSwitchView: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        startPostponedEnterTransition()

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        permissionLabelModeSwitchView = view.findViewById(R.id.configuration_show_permission_label)
        textViewXmlViewerSwitchView = view.findViewById(R.id.configuration_use_text_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keepScreenOnSwitchView.setChecked(ConfigurationPreferences.isKeepScreenOn())
        permissionLabelModeSwitchView.setChecked(ConfigurationPreferences.getPermissionLabelMode())
        textViewXmlViewerSwitchView.setChecked(ConfigurationPreferences.isXmlViewerTextView())

        keepScreenOnSwitchView.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                ConfigurationPreferences.setKeepScreenOn(isChecked)

                if (isChecked) {
                    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        })

        permissionLabelModeSwitchView.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                ConfigurationPreferences.setPermissionLabelMode(isChecked)
            }
        })

        textViewXmlViewerSwitchView.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                ConfigurationPreferences.setXmlViewerTextView(isChecked)
            }
        })
    }

    companion object {
        fun newInstance(): ConfigurationScreen {
            val args = Bundle()
            val fragment = ConfigurationScreen()
            fragment.arguments = args
            return fragment
        }
    }
}