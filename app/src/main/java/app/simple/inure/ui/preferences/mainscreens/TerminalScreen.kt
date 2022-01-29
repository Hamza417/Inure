package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.terminal.PopupInputMethod
import app.simple.inure.preferences.TerminalPreferences
import app.simple.inure.ui.preferences.subscreens.*
import app.simple.inure.util.FragmentHelper

class TerminalScreen : ScopedFragment() {

    private lateinit var fontSize: DynamicRippleRelativeLayout
    private lateinit var color: DynamicRippleRelativeLayout
    private lateinit var cursorBlink: SwitchView
    private lateinit var utf8: SwitchView
    private lateinit var backButtonAction: DynamicRippleRelativeLayout
    private lateinit var controlKey: DynamicRippleRelativeLayout
    private lateinit var fnKey: DynamicRippleRelativeLayout
    private lateinit var inputMethod: DynamicRippleTextView
    private lateinit var altKey: SwitchView
    private lateinit var keyboardShortcut: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_terminal, container, false)

        fontSize = view.findViewById(R.id.terminal_font_size)
        color = view.findViewById(R.id.terminal_color)
        cursorBlink = view.findViewById(R.id.terminal_cursor_blink_switch)
        utf8 = view.findViewById(R.id.terminal_utf_switch)
        backButtonAction = view.findViewById(R.id.terminal_back_button_behavior)
        controlKey = view.findViewById(R.id.terminal_control_key)
        fnKey = view.findViewById(R.id.terminal_fn_key)
        inputMethod = view.findViewById(R.id.popup_input_method)
        altKey = view.findViewById(R.id.alt_key_switch)
        keyboardShortcut = view.findViewById(R.id.keyboard_shortcuts_switch)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        cursorBlink.setChecked(TerminalPreferences.getCursorBlinkState())
        utf8.setChecked(TerminalPreferences.getUTF8State())
        altKey.setChecked(TerminalPreferences.getAltKeyEscapeState())
        keyboardShortcut.setChecked(TerminalPreferences.getKeyboardShortcutState())
        setInputMethodText()

        fontSize.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalFontSize.newInstance(), "font_size")
        }

        color.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalColor.newInstance(), "color")
        }

        cursorBlink.setOnSwitchCheckedChangeListener {
            TerminalPreferences.setCursorBlinkState(it)
        }

        utf8.setOnSwitchCheckedChangeListener {
            TerminalPreferences.setUTF8State(it)
        }

        backButtonAction.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalBackButtonAction.newInstance(), "back_button")
        }

        controlKey.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalControlKey.newInstance(), "control_key")
        }

        fnKey.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalFnKey.newInstance(), "fn_key")
        }

        inputMethod.setOnClickListener {
            PopupInputMethod(it)
        }

        altKey.setOnSwitchCheckedChangeListener {
            TerminalPreferences.setAltKeyEscapeState(it)
        }

        keyboardShortcut.setOnSwitchCheckedChangeListener {
            TerminalPreferences.setKeyboardShortcutState(it)
        }
    }

    private fun setInputMethodText() {
        inputMethod.text = when (TerminalPreferences.getInputMethod()) {
            0 -> getString(R.string.character_based)
            1 -> getString(R.string.word_based)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TerminalPreferences.inputMethod -> {
                setInputMethodText()
            }
        }
    }

    companion object {
        fun newInstance(): TerminalScreen {
            val args = Bundle()
            val fragment = TerminalScreen()
            fragment.arguments = args
            return fragment
        }
    }
}