package app.simple.inure.dialogs.terminal

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.TerminalConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.terminal.TerminalAddShortcutCallbacks

class TerminalAddShortcut : ScopedBottomSheetFragment() {

    private lateinit var command: TypeFaceEditText
    private lateinit var args: TypeFaceEditText
    private lateinit var label: TypeFaceEditText
    private lateinit var quoteForBash: CheckBox
    private lateinit var save: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var terminalAddShortcutCallbacks: TerminalAddShortcutCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_terminal_add_shortcut, container, false)

        command = view.findViewById(R.id.command_edit_text)
        args = view.findViewById(R.id.args_edit_text)
        label = view.findViewById(R.id.label_edit_text)
        quoteForBash = view.findViewById(R.id.quote_checkbox)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.hint = TerminalConstants.getRandomCommandHint()

        args.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                var s: String = args.text.toString()
                if (label.text.toString() == "" && args.text.toString().also { s = it } != "") {
                    label.setText(s.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                }
            }
        }

        save.setOnClickListener {
            terminalAddShortcutCallbacks
                ?.onShortcutAdded(
                        command.text.toString(),
                        args.text.toString(),
                        label.text.toString(),
                        quoteForBash.isChecked)
        }

        cancel.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().finish()
    }

    fun setTerminalAddShortcutCallbacks(terminalAddShortcutCallbacks: TerminalAddShortcutCallbacks) {
        this.terminalAddShortcutCallbacks = terminalAddShortcutCallbacks
    }

    companion object {
        fun newInstance(): TerminalAddShortcut {
            val args = Bundle()
            val fragment = TerminalAddShortcut()
            fragment.arguments = args
            return fragment
        }
    }
}