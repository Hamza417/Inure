package app.simple.inure.dialogs.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedDialogFragment
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class DialogContextMenu : ScopedDialogFragment() {

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

        if (requireArguments().getBoolean(BundleConstants.canPaste)) {
            paste.visible(false)
        } else {
            paste.gone()
        }

        selectText.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(0)
            dismiss()
        }

        copyAll.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(1)
            dismiss()
        }

        paste.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(2)
            dismiss()
        }

        sendControlKey.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(3)
            dismiss()
        }

        sendFnKey.setOnClickListener {
            terminalContextMenuCallbacks?.onMenuClicked(4)
            dismiss()
        }
    }

    fun setOnTerminalContextMenuCallbackListener(terminalContextMenuCallbacks: TerminalContextMenuCallbacks) {
        this.terminalContextMenuCallbacks = terminalContextMenuCallbacks
    }

    companion object {
        fun newInstance(canPaste: Boolean): DialogContextMenu {
            val args = Bundle()
            args.putBoolean(BundleConstants.canPaste, canPaste)
            val fragment = DialogContextMenu()
            fragment.arguments = args
            return fragment
        }

        interface TerminalContextMenuCallbacks {
            fun onMenuClicked(source: Int)
        }
    }
}