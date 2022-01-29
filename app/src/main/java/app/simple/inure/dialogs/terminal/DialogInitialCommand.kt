package app.simple.inure.dialogs.terminal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.extension.fragments.ScopedDialogFragment
import app.simple.inure.preferences.ShellPreferences

class DialogInitialCommand : ScopedDialogFragment() {

    private lateinit var command: TypeFaceEditTextDynamicCorner
    private lateinit var save: DynamicRippleTextView
    private lateinit var reset: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_initial_command, container, false)

        command = view.findViewById(R.id.command_edit_text)
        save = view.findViewById(R.id.save)
        reset = view.findViewById(R.id.reset)

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.setText(ShellPreferences.getInitialCommand())

        save.setOnClickListener {
            if (ShellPreferences.setInitialCommand(command.text.toString())) {
                dismiss()
            }
        }

        reset.setOnClickListener {
            command.setText("")
        }
    }

    companion object {
        fun newInstance(): DialogInitialCommand {
            val args = Bundle()
            val fragment = DialogInitialCommand()
            fragment.arguments = args
            return fragment
        }
    }
}