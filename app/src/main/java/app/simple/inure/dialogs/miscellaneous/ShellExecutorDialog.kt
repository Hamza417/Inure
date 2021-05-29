package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.exception.InureShellException
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import com.topjohnwu.superuser.Shell

class ShellExecutorDialog : ScopedBottomSheetFragment() {

    private lateinit var command: TypeFaceTextView
    private lateinit var result: TypeFaceEditText
    private var commandResultCallbacks: CommandResultCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_shell_executor, container, false)

        command = view.findViewById(R.id.shell_command)
        result = view.findViewById(R.id.shell_result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.text = requireArguments().getString("command")!!.replace("&", "\n")

        kotlin.runCatching {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(Shell.Builder.create()
                                            .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                            .setTimeout(10)
            )
            Shell.su(requireArguments().getString("command")!!).submit {
                kotlin.runCatching {
                    for (i in it.out) {
                        updateResult("\n" + i)
                        if (i.contains("Exception")) {
                            throw InureShellException("Execution Failed...")
                        }
                    }
                }.onSuccess {
                    result.append("\n${getString(R.string.done)}")
                    commandResultCallbacks?.onCommandExecuted(getString(R.string.done))
                }.getOrElse {
                    updateResult("\n" + it.message!!)
                }
            }

        }.onFailure {
            updateResult("\n" + it.message!!)
        }.getOrElse {
            updateResult("\n" + it.message!!)
        }
    }

    private fun updateResult(output: String) {
        this@ShellExecutorDialog.result.append(output)
        commandResultCallbacks?.onCommandExecuted(output)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shell.getShell().close()
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