package app.simple.inure.ui.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.decorations.terminal.TerminalService
import app.simple.inure.decorations.terminal.TerminalService.ServiceBinder
import app.simple.inure.decorations.terminal.TerminalView
import app.simple.inure.extension.fragments.ScopedFragment
import java.io.DataOutputStream


class Terminal : ScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var terminal: TerminalView
    private lateinit var animatedVectorDrawable: AnimatedVectorDrawable

    private lateinit var suProcess: Process
    private lateinit var outputStream: DataOutputStream
    private lateinit var terminalService: TerminalService
    private lateinit var serviceConnection: ServiceConnection
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        icon = view.findViewById(R.id.terminal_header_icon)
        terminal = view.findViewById(R.id.terminal_view)
        animatedVectorDrawable = icon.drawable as AnimatedVectorDrawable
        //handler.post(animatorRunnable)

        startPostponedEnterTransition()

        println("Created")

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                terminalService = (service as ServiceBinder).service
                terminalService.createTerminal()
                terminal.terminal = terminalService.terminals.valueAt(0)
                terminal.requestFocus()
                container?.addView(terminal)
                println("Service connected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }
        }

        requireActivity().bindService(
            Intent(requireContext(), TerminalService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        println("Start")

    }

    private val animatorRunnable = object : Runnable {
        override fun run() {
           animatedVectorDrawable.start()
            handler.postDelayed(this, 1000L)
        }
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