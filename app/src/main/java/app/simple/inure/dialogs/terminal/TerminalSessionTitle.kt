package app.simple.inure.dialogs.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.terminal.TerminalSessionTitleCallbacks

class TerminalSessionTitle : ScopedBottomSheetFragment() {

    private lateinit var title: DynamicCornerEditText
    private lateinit var save: DynamicRippleTextView

    private var terminalSessionTitleCallbacks: TerminalSessionTitleCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_terminal_session_title, container, false)

        title = view.findViewById(R.id.title_et)
        save = view.findViewById(R.id.save)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            terminalSessionTitleCallbacks?.onTitleChanged(title.text.toString())
            dismiss()
        }

        title.showInput()
    }

    fun setTerminalSessionTitleCallbacks(terminalSessionTitleCallbacks: TerminalSessionTitleCallbacks) {
        this.terminalSessionTitleCallbacks = terminalSessionTitleCallbacks
    }

    companion object {
        fun newInstance(): TerminalSessionTitle {
            val args = Bundle()
            val fragment = TerminalSessionTitle()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTerminalSessionTitleDialog(terminalSessionTitleCallbacks: TerminalSessionTitleCallbacks): TerminalSessionTitle {
            val terminalSessionTitle = newInstance()
            terminalSessionTitle.setTerminalSessionTitleCallbacks(terminalSessionTitleCallbacks)
            terminalSessionTitle.show(this, "terminal_session_title")
            return terminalSessionTitle
        }
    }
}