package app.simple.inure.dialogs.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedDialogFragment

class DialogCloseWindow : ScopedDialogFragment() {

    private lateinit var ok: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView
    private var terminalCloseDialogCallback: TerminalCloseDialogCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_terminal_close, container, false)

        ok = view.findViewById(R.id.ok)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancel.setOnClickListener {
            dismiss()
        }

        ok.setOnClickListener {
            terminalCloseDialogCallback?.onClose().also {
                dismiss()
            }
        }
    }

    fun setOnTerminalDialogCloseListener(terminalCloseDialogCallback: TerminalCloseDialogCallback) {
        this.terminalCloseDialogCallback = terminalCloseDialogCallback
    }

    companion object {
        fun newInstance(): DialogCloseWindow {
            val args = Bundle()
            val fragment = DialogCloseWindow()
            fragment.arguments = args
            return fragment
        }

        interface TerminalCloseDialogCallback {
            fun onClose()
        }
    }
}