package app.simple.inure.ui.panels

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.terminal.TerminalView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupTerminal
import app.simple.inure.services.TerminalService
import app.simple.inure.services.TerminalService.ServiceBinder
import app.simple.inure.util.TypeFace

class Terminal : ScopedFragment() {

    private lateinit var terminalOptions: DynamicRippleImageButton
    private lateinit var terminal: TerminalView

    private lateinit var terminalService: TerminalService
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        terminal = view.findViewById(R.id.terminal_view)
        terminalOptions = view.findViewById(R.id.terminal_options_button)

        startPostponedEnterTransition()

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                terminalService = (service as ServiceBinder).service
                terminalService.createTerminal()

                if (terminalService.terminals.size() > 0) {
                    terminal.terminal = terminalService.terminals.valueAt(0)
                } else {
                    val e = ErrorPopup.newInstance("${requireContext().packageName}: Could not create terminal")
                    e.show(childFragmentManager, "error_dialog")
                    e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                        override fun onDismiss() {
                            requireActivity().onBackPressed()
                        }
                    })
                }

                terminal.scrollToBottom(false)

                /**
                 * Uncomment this line if you want the terminal to use the default
                 * [TypeFace] app is using. It can create certain problems that is
                 * fonts used in this app have very limited charset and there whitespaces
                 * can make the information shown in the terminal window inconsistent
                 */
                // terminal.setTypeFace(TypeFace.getTypeFace(AppearancePreferences.getAppFont(), 1, requireContext()))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        packageInfo.applicationInfo.sharedLibraryFiles

        requireActivity().bindService(
            Intent(requireContext(), TerminalService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)

        terminalOptions.setOnClickListener {
            PopupTerminal(it).setOnMenuClickListener(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    when (source) {
                        getString(R.string.kill) -> {
                            terminalService.destroyTerminal(terminal.terminal.key)
                            requireActivity().onBackPressed()
                        }
                        getString(R.string.close) -> {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(serviceConnection)
    }

    companion object {
        fun newInstance(): Terminal {
            val args = Bundle()
            val fragment = Terminal()
            fragment.arguments = args
            return fragment
        }
    }
}