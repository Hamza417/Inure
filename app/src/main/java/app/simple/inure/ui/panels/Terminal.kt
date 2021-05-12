package app.simple.inure.ui.panels

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.terminal.TerminalService
import app.simple.inure.decorations.terminal.TerminalService.ServiceBinder
import app.simple.inure.decorations.terminal.TerminalView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupTerminal
import app.simple.inure.util.TypeFace

class Terminal : ScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var terminalOptions: DynamicRippleImageButton
    private lateinit var terminal: TerminalView

    private lateinit var terminalService: TerminalService
    private lateinit var serviceConnection: ServiceConnection
    private var isKilled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        icon = view.findViewById(R.id.terminal_header_icon)
        terminal = view.findViewById(R.id.terminal_view)
        terminalOptions = view.findViewById(R.id.terminal_options_button)
        (icon.drawable as AnimatedVectorDrawable).start()

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                terminalService = (service as ServiceBinder).service
                terminalService.createTerminal()

                if (terminalService.terminals.size() > 0) {
                    terminal.terminal = terminalService.terminals.valueAt(0)
                } else {
                    Log.d(requireContext().packageName, "Could not create terminal")
                }

                terminal.requestFocus()
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

        requireActivity().bindService(
            Intent(requireContext(), TerminalService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)

        terminalOptions.setOnClickListener {
            val popup = PopupTerminal(layoutInflater.inflate(R.layout.popup_terminal_menu, PopupLinearLayout(requireContext()), true), it)

            popup.setOnMenuClickListener(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    when (source) {
                        getString(R.string.kill) -> {
                            isKilled = true
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
        if(isKilled) {
            terminalService.destroyTerminal(terminal.terminal.key)
        }
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
