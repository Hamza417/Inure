package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.dialog.ShellExecutorViewModelFactory
import app.simple.inure.viewmodels.dialogs.ShellExecutorViewModel

class ShellExecutorDialog : ScopedBottomSheetFragment() {

    private lateinit var command: TypeFaceTextView
    private lateinit var result: TypeFaceEditText
    private lateinit var shellExecutorViewModel: ShellExecutorViewModel
    private lateinit var shellExecutorViewModelFactory: ShellExecutorViewModelFactory
    private var commandResultCallbacks: CommandResultCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_shell_executor, container, false)

        command = view.findViewById(R.id.shell_command)
        result = view.findViewById(R.id.shell_result)

        shellExecutorViewModelFactory = ShellExecutorViewModelFactory(requireArguments().getString("command")!!, requireActivity().application)
        shellExecutorViewModel = ViewModelProvider(this, shellExecutorViewModelFactory).get(ShellExecutorViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.text = requireArguments().getString("command")!!.replace("&", "\n")

        shellExecutorViewModel.getResults().observe(viewLifecycleOwner, {
            updateResult(it)
        })

        shellExecutorViewModel.getSuccessStatus().observe(viewLifecycleOwner, {
            if (it.contains("Done")) {
                result.append("\n${getString(R.string.done)}")
                commandResultCallbacks?.onCommandExecuted(getString(R.string.done))
            }
        })
    }

    private fun updateResult(output: String) {
        this@ShellExecutorDialog.result.append(output)
        commandResultCallbacks?.onCommandExecuted(output)
    }

    fun setOnCommandResultListener(commandResultCallbacks: CommandResultCallbacks) {
        this.commandResultCallbacks = commandResultCallbacks
    }

    companion object {
        fun newInstance(command: String): ShellExecutorDialog {
            val args = Bundle()
            args.putString("command", command)
            val fragment = ShellExecutorDialog()
            fragment.arguments = args
            return fragment
        }

        interface CommandResultCallbacks {
            fun onCommandExecuted(result: String)
        }
    }
}