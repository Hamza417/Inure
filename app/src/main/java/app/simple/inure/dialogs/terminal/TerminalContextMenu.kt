package app.simple.inure.dialogs.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompatFactory
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.util.ViewUtils.visible

class TerminalContextMenu : ScopedDialogFragment() {

    private lateinit var selectText: DynamicRippleTextView
    private lateinit var copyAll: DynamicRippleTextView
    private lateinit var paste: DynamicRippleTextView
    private lateinit var sendControlKey: DynamicRippleTextView
    private lateinit var sendFnKey: DynamicRippleTextView
    private var terminalContextMenuCallbacks: TerminalContextMenuCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_terminal_context_menu, container, false)

        selectText = view.findViewById(R.id.select_text)
        copyAll = view.findViewById(R.id.copy_all)
        paste = view.findViewById(R.id.paste)
        sendControlKey = view.findViewById(R.id.send_control_key)
        sendFnKey = view.findViewById(R.id.send_fn_key)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectText.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(SELECT_TEXT_ID)
            dismiss()
        }

        copyAll.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(COPY_ALL_ID)
            dismiss()
        }

        paste.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(PASTE_ID)
            dismiss()
        }

        sendControlKey.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(SEND_CONTROL_KEY_ID)
            dismiss()
        }

        sendFnKey.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(SEND_FN_KEY_ID)
            dismiss()
        }

        pasteState()
    }

    private fun pasteState() {
        if (canPaste()) {
            paste.visible(false)
        } else {
            paste.isClickable = false
            paste.alpha = 0.4F
        }
    }

    fun setOnTerminalContextMenuCallbackListener(terminalContextMenuCallbacks: TerminalContextMenuCallbacks) {
        this.terminalContextMenuCallbacks = terminalContextMenuCallbacks
    }

    private fun canPaste(): Boolean {
        val clip = ClipboardManagerCompatFactory.getManager(requireContext())
        return clip.hasText()
    }

    override fun onResume() {
        super.onResume()
        pasteState()
    }

    companion object {
        fun newInstance(): TerminalContextMenu {
            val args = Bundle()
            val fragment = TerminalContextMenu()
            fragment.arguments = args
            return fragment
        }

        interface TerminalContextMenuCallbacks {
            fun onMenuClicked(source: Int)
        }

        const val SELECT_TEXT_ID = 0
        const val COPY_ALL_ID = 1
        const val PASTE_ID = 2
        const val SEND_CONTROL_KEY_ID = 3
        const val SEND_FN_KEY_ID = 4
    }
}
