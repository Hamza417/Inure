package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.dialogs.terminal.DialogCommandLine
import app.simple.inure.dialogs.terminal.DialogHomePath
import app.simple.inure.dialogs.terminal.DialogInitialCommand
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.ShellPreferences
import app.simple.inure.ui.preferences.subscreens.ShellTerminalType

class ShellScreen : ScopedFragment() {

    private lateinit var commandLine: DynamicRippleRelativeLayout
    private lateinit var initialCommand: DynamicRippleRelativeLayout
    private lateinit var terminalType: DynamicRippleRelativeLayout
    private lateinit var sendMouseEvent: SwitchView
    private lateinit var closeWindow: SwitchView
    private lateinit var verifyPathEntries: SwitchView
    private lateinit var allowPathExtensions: SwitchView
    private lateinit var allowPathPrepend: SwitchView
    private lateinit var homePath: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_shell, container, false)

        commandLine = view.findViewById(R.id.command_line)
        initialCommand = view.findViewById(R.id.initial_command)
        terminalType = view.findViewById(R.id.terminal_type)
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

        sendMouseEvent.setChecked(ShellPreferences.getMouseEventState())
        closeWindow.setChecked(ShellPreferences.getCloseWindowOnExitState())
        verifyPathEntries.setChecked(ShellPreferences.getVerifyPathEntriesState())
        allowPathExtensions.setChecked(ShellPreferences.getAllowPathExtensionsState())
        allowPathPrepend.setChecked(ShellPreferences.getAllowPathPrependState())

        commandLine.setOnClickListener {
            DialogCommandLine.newInstance()
                .show(childFragmentManager, "command_line")
        }

        initialCommand.setOnClickListener {
            DialogInitialCommand.newInstance()
                .show(childFragmentManager, "initial_command")
        }

        terminalType.setOnClickListener {
            openFragmentSlide(ShellTerminalType.newInstance(), "terminal_type")
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
            DialogHomePath.newInstance()
                .show(childFragmentManager, "home_path")
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