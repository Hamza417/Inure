package app.simple.inure.dialogs.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.TerminalConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.terminal.TerminalAddShortcutCallbacks
import app.simple.inure.models.TerminalCommand
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ParcelUtils.parcelable

class TerminalCreateShortcut : ScopedBottomSheetFragment() {

    private lateinit var command: TypeFaceEditText
    private lateinit var args: TypeFaceEditText
    private lateinit var label: TypeFaceEditText
    private lateinit var quoteForBash: CheckBox
    private lateinit var description: TypeFaceEditText
    private lateinit var save: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var terminalAddShortcutCallbacks: TerminalAddShortcutCallbacks? = null
    private var terminalCommand: TerminalCommand? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_create_terminal_shortcut, container, false)

        command = view.findViewById(R.id.command_edit_text)
        args = view.findViewById(R.id.args_edit_text)
        label = view.findViewById(R.id.label_edit_text)
        quoteForBash = view.findViewById(R.id.quote_checkbox)
        description = view.findViewById(R.id.description_edit_text)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        terminalCommand = requireArguments().parcelable(BundleConstants.TERMINAL_COMMAND)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (terminalCommand.isNotNull()) {
            command.setText(terminalCommand!!.command)
            args.setText(terminalCommand!!.arguments)
            label.setText(terminalCommand!!.label)
            description.setText(terminalCommand!!.description)
        } else {
            command.hint = TerminalConstants.getRandomCommandHint()
        }

        args.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                var s: String = args.text.toString()
                if (label.text.toString() == "" && args.text.toString().also { s = it } != "") {
                    label.setText(s.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                }
            }
        }

        save.setOnClickListener {
            val command = if (quoteForBash.isChecked) {
                quoteForBash(command.text.toString())
            } else {
                command.text.toString()
            }

            val args = args.text.toString()
            val label = label.text.toString().ifEmpty { command }

            terminalAddShortcutCallbacks?.onCreateShortcut(command, args, label, description.text.toString())
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Quote a string so it can be used as a parameter in bash and similar shells.
     */
    private fun quoteForBash(s: String): String {
        val builder = StringBuilder()
        val specialChars = "\"\\$`!"
        builder.append('"')
        val length = s.length
        for (i in 0 until length) {
            val c = s[i]
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\')
            }
            builder.append(c)
        }
        builder.append('"')
        return builder.toString()
    }

    fun setTerminalAddShortcutCallbacks(terminalAddShortcutCallbacks: TerminalAddShortcutCallbacks) {
        this.terminalAddShortcutCallbacks = terminalAddShortcutCallbacks
    }

    companion object {
        fun newInstance(): TerminalCreateShortcut {
            val args = Bundle()
            val fragment = TerminalCreateShortcut()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.createTerminalShortcut(): TerminalCreateShortcut {
            val fragment = newInstance()
            fragment.show(this, "create_terminal_shortcut")
            return fragment
        }

        fun FragmentManager.editTerminalShortcut(terminalCommand: TerminalCommand): TerminalCreateShortcut {
            val args = Bundle()
            args.putParcelable(BundleConstants.TERMINAL_COMMAND, terminalCommand)
            val fragment = TerminalCreateShortcut()
            fragment.arguments = args
            fragment.show(this, "edit_terminal_shortcut")
            return fragment
        }
    }
}