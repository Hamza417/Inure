package app.simple.inure.dialogs.miscellaneous

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                Shell.enableVerboseLogging = BuildConfig.DEBUG
                Shell.setDefaultBuilder(Shell.Builder.create()
                                                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                .setTimeout(10)
                )
                Shell.su(requireArguments().getString("command")!!).submit {
                    updateUI(it)
                }
            }.getOrElse {
                result.setText(it.stackTraceToString())
            }
        }
    }

    private fun updateUI(result: Shell.Result) {
        viewLifecycleOwner.lifecycleScope.launch {
            for (i in result.out.indices) {
                @SuppressLint("SetTextI18n")
                if (this@ShellExecutorDialog.result.text.isNullOrEmpty()) {
                    this@ShellExecutorDialog.result.setText(result.out[i])
                } else {
                    this@ShellExecutorDialog.result.setText(this@ShellExecutorDialog.result.text.toString() + "\n" + result.out[i])
                }

                commandResultCallbacks?.onCommandExecuted(result.out[i])
            }
        }
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