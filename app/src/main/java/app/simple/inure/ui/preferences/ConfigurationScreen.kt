package app.simple.inure.ui.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.activities.MainActivity
import app.simple.inure.decorations.switchview.SwitchCallbacks
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.SDKHelper
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils

class ConfigurationScreen : ScopedFragment() {

    private lateinit var keepScreenOnSwitchView: SwitchView
    private lateinit var permissionLabelModeSwitchView: SwitchView
    private lateinit var textViewXmlViewerSwitchView: SwitchView
    private lateinit var rootSwitchView: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configuration, container, false)

        startPostponedEnterTransition()

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        permissionLabelModeSwitchView = view.findViewById(R.id.configuration_show_permission_label)
        textViewXmlViewerSwitchView = view.findViewById(R.id.configuration_use_text_view)
        rootSwitchView = view.findViewById(R.id.configuration_root_switch_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keepScreenOnSwitchView.setChecked(ConfigurationPreferences.isKeepScreenOn())
        permissionLabelModeSwitchView.setChecked(ConfigurationPreferences.getPermissionLabelMode())
        textViewXmlViewerSwitchView.setChecked(ConfigurationPreferences.isXmlViewerTextView())

        rootSwitchView.setChecked(Shell.getShell().isRoot)

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
            // Set settings before the main shell can be created
            Shell.enableVerboseLogging = BuildConfig.DEBUG;
            Shell.setDefaultBuilder(Shell.Builder.create()
                                            .setFlags(Shell.FLAG_REDIRECT_STDERR)
                                            .setTimeout(10)
            )

            // Preheat the main root shell in the splash screen
            // so the app can use it afterwards without interrupting
            // application flow (e.g. root permission prompt)
            Shell.getShell { shell: Shell? ->
                when(shell?.status) {
                    Shell.UNKNOWN -> {
                        ConfigurationPreferences.setUsingRoot(false)
                    }
                    Shell.NON_ROOT_SHELL -> {
                        ConfigurationPreferences.setUsingRoot(false)
                    }
                    Shell.ROOT_SHELL -> {
                        ConfigurationPreferences.setUsingRoot(true)
                    }
                }
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