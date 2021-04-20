package app.simple.inure.ui.preferences

import android.os.Bundle
import android.view.*
import app.simple.inure.R
import app.simple.inure.decorations.switch.SwitchCallbacks
import app.simple.inure.decorations.switch.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.MainPreferences

class ConfigurationScreen : ScopedFragment() {

    private lateinit var keepScreenOnSwitchView: SwitchView
    private lateinit var permissionLabelModeSwitchView : SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        startPostponedEnterTransition()

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        permissionLabelModeSwitchView = view.findViewById(R.id.configuration_show_permission_label)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keepScreenOnSwitchView.setChecked(MainPreferences.isKeepScreenOn())
        permissionLabelModeSwitchView.setChecked(MainPreferences.getPermissionLabelMode())

        keepScreenOnSwitchView.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                MainPreferences.setKeepScreenOn(isChecked)

                if (isChecked) {
                    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        })

        permissionLabelModeSwitchView.setOnSwitchCheckedChangeListener(object : SwitchCallbacks {
            override fun onCheckedChanged(isChecked: Boolean) {
                MainPreferences.setPermissionLabelMode(isChecked)
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