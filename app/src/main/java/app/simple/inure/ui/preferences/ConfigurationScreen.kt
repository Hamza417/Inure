package app.simple.inure.ui.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.MainPreferences
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils

class ConfigurationScreen : ScopedFragment() {

    private lateinit var keepScreenOnSwitchView: SwitchView
    private lateinit var permissionLabelModeSwitchView: SwitchView
    private lateinit var textViewXmlViewerSwitchView: SwitchView
    private lateinit var useBinaryFormat: SwitchView
    private lateinit var rootSwitchView: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        startPostponedEnterTransition()

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        permissionLabelModeSwitchView = view.findViewById(R.id.configuration_show_permission_label)
        textViewXmlViewerSwitchView = view.findViewById(R.id.configuration_use_text_view)
        useBinaryFormat = view.findViewById(R.id.configuration_use_binary_format)
        rootSwitchView = view.findViewById(R.id.configuration_root_switch_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keepScreenOnSwitchView.setChecked(ConfigurationPreferences.isKeepScreenOn())
        permissionLabelModeSwitchView.setChecked(ConfigurationPreferences.getPermissionLabelMode())
        textViewXmlViewerSwitchView.setChecked(ConfigurationPreferences.isXmlViewerTextView())
        useBinaryFormat.setChecked(ConfigurationPreferences.getSizeType() == "binary")

        rootSwitchView.setChecked(ConfigurationPreferences.isUsingRoot())

        keepScreenOnSwitchView.setOnSwitchCheckedChangeListener { isChecked ->
            ConfigurationPreferences.setKeepScreenOn(isChecked)

            if (isChecked) {
                requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        permissionLabelModeSwitchView.setOnSwitchCheckedChangeListener { isChecked ->
            ConfigurationPreferences.setPermissionLabelMode(isChecked)
        }

        textViewXmlViewerSwitchView.setOnSwitchCheckedChangeListener { isChecked ->
            ConfigurationPreferences.setXmlViewerTextView(isChecked)
        }

        rootSwitchView.setOnSwitchCheckedChangeListener {
            if(it && Shell.getShell().isRoot) {
                ConfigurationPreferences.setUsingRoot(true)
            } else {
                ConfigurationPreferences.setUsingRoot(false)
            }
        }

        useBinaryFormat.setOnSwitchCheckedChangeListener {
            if(it) {
                ConfigurationPreferences.setSizeType("binary")
            } else {
                ConfigurationPreferences.setSizeType("si")
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            ConfigurationPreferences.isUsingRoot -> {
                rootSwitchView.setChecked(ConfigurationPreferences.isUsingRoot())
            }
        }
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