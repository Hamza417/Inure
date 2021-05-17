package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShellExecutorDialog : ScopedBottomSheetFragment() {

    private lateinit var command: TypeFaceTextView
    private lateinit var result: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_shell_executor, container, false)

        command = view.findViewById(R.id.shell_command)
        result = view.findViewById(R.id.shell_result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.text = requireArguments().getString("command")

        viewLifecycleOwner.lifecycleScope.launch {
            val output = withContext(Dispatchers.IO) {
                ShellUtils.fastCmd("su")
                ShellUtils.fastCmd(requireArguments().getString("command"))
            }

            result.text = output
        }
    }

    companion object {
        fun newInstance(command: String): ShellExecutorDialog {
            val args = Bundle()
            args.putString("command", command)
            val fragment = ShellExecutorDialog()
            fragment.arguments = args
            return fragment
        }
    }
}