package app.simple.inure.dialogs.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedDialogFragment

class DialogTerminalMainMenu : ScopedDialogFragment() {

    private lateinit var windows: DynamicRippleTextView
    private lateinit var toggleKeyboard: DynamicRippleTextView
    private lateinit var specialKeys: DynamicRippleTextView
    private lateinit var preferences: DynamicRippleTextView
    private lateinit var reset: DynamicRippleTextView
    private lateinit var copy: DynamicRippleTextView
    private lateinit var wakeLock: DynamicRippleTextView
    private lateinit var wifiLock: DynamicRippleTextView

    private var terminalMenuCallbacks: TerminalMenuCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_terminal_menu, container, false)

        windows = view.findViewById(R.id.terminal_windows)
        toggleKeyboard = view.findViewById(R.id.terminal_toggle_keyboard)
        specialKeys = view.findViewById(R.id.terminal_special_keys)
        preferences = view.findViewById(R.id.terminal_preferences)
        reset = view.findViewById(R.id.terminal_reset)
        copy = view.findViewById(R.id.terminal_copy)
        wakeLock = view.findViewById(R.id.terminal_wake_lock)
        wifiLock = view.findViewById(R.id.terminal_wifi_lock)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getBoolean(BundleConstants.wakelock)) {
            wakeLock.text = requireContext().getString(R.string.disable_wakelock)
        } else {
            wakeLock.text = requireContext().getString(R.string.enable_wakelock)
        }

        if (requireArguments().getBoolean(BundleConstants.wifilock)) {
            wifiLock.text = requireContext().getString(R.string.disable_wifilock)
        } else {
            wifiLock.text = requireContext().getString(R.string.enable_wifilock)
        }

        windows.onClick(0)
        toggleKeyboard.onClick(1)
        specialKeys.onClick(2)
        preferences.onClick(3)
        reset.onClick(4)
        copy.onClick(5)
        wakeLock.onClick(6)
        wifiLock.onClick(7)
    }

    private fun DynamicRippleTextView.onClick(source: Int) {
        this.setOnClickListener {
            dismiss()
            terminalMenuCallbacks?.onMenuClicked(source)
        }
    }

    fun setOnTerminalMenuCallbacksListener(terminalMenuCallbacks: TerminalMenuCallbacks) {
        this.terminalMenuCallbacks = terminalMenuCallbacks
    }

    companion object {
        fun newInstance(wakeLockHeld: Boolean, wifiLockHeld: Boolean): DialogTerminalMainMenu {
            val args = Bundle()
            args.putBoolean(BundleConstants.wakelock, wakeLockHeld)
            args.putBoolean(BundleConstants.wifilock, wifiLockHeld)
            val fragment = DialogTerminalMainMenu()
            fragment.arguments = args
            return fragment
        }

        interface TerminalMenuCallbacks {
            fun onMenuClicked(source: Int)
        }
    }
}