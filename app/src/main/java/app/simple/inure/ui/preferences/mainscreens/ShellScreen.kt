package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.terminal.TerminalCommandLine
import app.simple.inure.dialogs.terminal.TerminalHomePath
import app.simple.inure.dialogs.terminal.TerminalInitialCommand
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.ShellPreferences
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.ui.preferences.subscreens.ShellTerminalType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShellScreen : ScopedFragment() {

    private lateinit var commandLine: DynamicRippleRelativeLayout
    private lateinit var initialCommand: DynamicRippleRelativeLayout
    private lateinit var terminalType: DynamicRippleRelativeLayout
    private lateinit var useRISH: Switch
    private lateinit var sendMouseEvent: Switch
    private lateinit var closeWindow: Switch
    private lateinit var verifyPathEntries: Switch
    private lateinit var allowPathExtensions: Switch
    private lateinit var allowPathPrepend: Switch
    private lateinit var homePath: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_shell, container, false)

        commandLine = view.findViewById(R.id.command_line)
        initialCommand = view.findViewById(R.id.initial_command)
        terminalType = view.findViewById(R.id.terminal_type)
        useRISH = view.findViewById(R.id.rish_switch)
        sendMouseEvent = view.findViewById(R.id.mouse_event_switch)
        closeWindow = view.findViewById(R.id.close_window_switch)
        verifyPathEntries = view.findViewById(R.id.verify_path_entries_switch)
        allowPathExtensions = view.findViewById(R.id.verify_path_extensions_switch)
        allowPathPrepend = view.findViewById(R.id.allow_path_prepend_switch)
        homePath = view.findViewById(R.id.home_path)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        fullVersionCheck()

        useRISH.isChecked = ShellPreferences.isUsingRISH()
        sendMouseEvent.isChecked = ShellPreferences.getMouseEventState()
        closeWindow.isChecked = ShellPreferences.getCloseWindowOnExitState()
        verifyPathEntries.isChecked = ShellPreferences.getVerifyPathEntriesState()
        allowPathExtensions.isChecked = ShellPreferences.getAllowPathExtensionsState()
        allowPathPrepend.isChecked = ShellPreferences.getAllowPathPrependState()

        commandLine.setOnClickListener {
            TerminalCommandLine.newInstance()
                .show(childFragmentManager, "command_line")
        }

        initialCommand.setOnClickListener {
            TerminalInitialCommand.newInstance()
                .show(childFragmentManager, "initial_command")
        }

        terminalType.setOnClickListener {
            openFragmentSlide(ShellTerminalType.newInstance(), "terminal_type")
        }

        useRISH.setOnSwitchCheckedChangeListener {
            ShellPreferences.setUseRISH(it)
        }

        sendMouseEvent.setOnSwitchCheckedChangeListener {
            ShellPreferences.setMouseEventState(it)
        }

        closeWindow.setOnSwitchCheckedChangeListener {
            ShellPreferences.setCloseWindowOnExitState(it)
        }

        verifyPathEntries.setOnSwitchCheckedChangeListener {
            ShellPreferences.setVerifyPathEntriesState(it)
        }

        allowPathExtensions.setOnSwitchCheckedChangeListener {
            ShellPreferences.setAllowPathExtensionsState(it)
        }

        allowPathPrepend.setOnSwitchCheckedChangeListener {
            ShellPreferences.setAllowPathPrependState(it)
        }

        homePath.setOnClickListener {
            TerminalHomePath.newInstance()
                .show(childFragmentManager, "home_path")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ShellPreferences.useRish -> {
                if (ShellPreferences.isUsingRISH()) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        ShizukuUtils.copyRishFiles(requireActivity().applicationContext)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): ShellScreen {
            val args = Bundle()
            val fragment = ShellScreen()
            fragment.arguments = args
            return fragment
        }
    }
}